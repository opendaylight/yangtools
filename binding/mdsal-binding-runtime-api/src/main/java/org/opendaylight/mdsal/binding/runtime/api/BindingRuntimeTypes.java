/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * The result of BindingGenerator run. Contains mapping between Types and SchemaNodes.
 */
@Beta
public final class BindingRuntimeTypes implements EffectiveModelContextProvider, Immutable {
    private static final VarHandle TYPE_TO_IDENTIFIER;

    static {
        try {
            TYPE_TO_IDENTIFIER = MethodHandles.lookup().findVarHandle(BindingRuntimeTypes.class, "typeToIdentifier",
                ImmutableMap.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull EffectiveModelContext schemaContext;
    private final ImmutableMap<Type, AugmentationSchemaNode> typeToAugmentation;
    private final ImmutableBiMap<Type, WithStatus> typeToSchema;
    private final ImmutableMultimap<Type, Type> choiceToCases;
    private final ImmutableMap<QName, Type> identities;

    @SuppressWarnings("unused")
    // Accessed via TYPE_TO_IDENTIFIER
    private volatile ImmutableMap<Type, Absolute> typeToIdentifier = ImmutableMap.of();

    public BindingRuntimeTypes(final EffectiveModelContext schemaContext,
            final Map<Type, AugmentationSchemaNode> typeToAugmentation,
            final BiMap<Type, WithStatus> typeToDefiningSchema, final Multimap<Type, Type> choiceToCases,
            final Map<QName, Type> identities) {
        this.schemaContext = requireNonNull(schemaContext);
        this.typeToAugmentation = ImmutableMap.copyOf(typeToAugmentation);
        this.typeToSchema = ImmutableBiMap.copyOf(typeToDefiningSchema);
        this.choiceToCases = ImmutableMultimap.copyOf(choiceToCases);
        this.identities = ImmutableMap.copyOf(identities);
    }

    @Override
    public EffectiveModelContext getEffectiveModelContext() {
        return schemaContext;
    }

    public Optional<AugmentationSchemaNode> findAugmentation(final Type type) {
        return Optional.ofNullable(typeToAugmentation.get(type));
    }

    public Optional<Type> findIdentity(final QName qname) {
        return Optional.ofNullable(identities.get(qname));
    }

    public Optional<WithStatus> findSchema(final Type type) {
        return Optional.ofNullable(typeToSchema.get(type));
    }

    public Optional<Absolute> findSchemaNodeIdentifier(final Type type) {
        final ImmutableMap<Type, Absolute> local = (ImmutableMap<Type, Absolute>) TYPE_TO_IDENTIFIER.getAcquire(this);
        final Absolute existing = local.get(type);
        return existing != null ? Optional.of(existing) : loadSchemaNodeIdentifier(local, type);
    }

    public Optional<Type> findType(final WithStatus schema) {
        return Optional.ofNullable(typeToSchema.inverse().get(schema));
    }

    public Multimap<Type, Type> getChoiceToCases() {
        return choiceToCases;
    }

    public Collection<Type> findCases(final Type choiceType) {
        return choiceToCases.get(choiceType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("typeToAugmentation", typeToAugmentation)
                .add("typeToSchema", typeToSchema)
                .add("choiceToCases", choiceToCases)
                .add("identities", identities)
                .toString();
    }

    private Optional<Absolute> loadSchemaNodeIdentifier(final ImmutableMap<Type, Absolute> local, final Type type) {
        final WithStatus schema = typeToSchema.get(type);
        if (!(schema instanceof SchemaNode)) {
            return Optional.empty();
        }

        // TODO: do not rely on getPath() here
        final Absolute created = Absolute.of(ImmutableList.copyOf(((SchemaNode) schema).getPath().getPathFromRoot()))
                .intern();

        ImmutableMap<Type, Absolute> prev = local;
        while (true) {
            // Compute next cache
            final ImmutableMap<Type, Absolute> next =
                    ImmutableMap.<Type, Absolute>builderWithExpectedSize(prev.size() + 1)
                        .putAll(prev)
                        .put(type, created).build();

            final Object witness = TYPE_TO_IDENTIFIER.compareAndExchangeRelease(this, prev, next);
            if (witness == prev) {
                // Cache populated successfully, we are all done now
                return Optional.of(created);
            }

            // Remember cache for next computation
            prev = (ImmutableMap<Type, Absolute>) witness;
            final Absolute raced = prev.get(type);
            if (raced != null) {
                // We have raced on this item, use it from cache
                return Optional.of(raced);
            }

            // We have raced on a different item, loop around and repeat
        }
    }
}
