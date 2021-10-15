/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The result of BindingGenerator run. Contains mapping between Types and SchemaNodes.
 */
@Beta
public final class BindingRuntimeTypes implements EffectiveModelContextProvider, Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeTypes.class);
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
    private final ImmutableMap<Type, WithStatus> typeToSchema;
    private final ImmutableMultimap<Type, Type> choiceToCases;
    private final ImmutableMap<QName, Type> identities;
    // Not Immutable as we use two different implementations
    private final Map<WithStatus, Type> schemaToType;

    @SuppressWarnings("unused")
    // Accessed via TYPE_TO_IDENTIFIER
    private volatile ImmutableMap<Type, Absolute> typeToIdentifier = ImmutableMap.of();

    public BindingRuntimeTypes(final EffectiveModelContext schemaContext,
            final Map<Type, AugmentationSchemaNode> typeToAugmentation,
            final Map<Type, WithStatus> typeToSchema, final Map<WithStatus, Type> schemaToType,
            final Map<QName, Type> identities) {
        this.schemaContext = requireNonNull(schemaContext);
        this.typeToAugmentation = ImmutableMap.copyOf(typeToAugmentation);
        this.typeToSchema = ImmutableMap.copyOf(typeToSchema);
        this.identities = ImmutableMap.copyOf(identities);

        // Careful to use identity for SchemaNodes, but only if needed
        // FIXME: 8.0.0: YT should be switching to identity for equals(), so this should become unnecessary
        Map<WithStatus, Type> copy;
        try {
            copy = ImmutableMap.copyOf(schemaToType);
        } catch (IllegalArgumentException e) {
            LOG.debug("Equality-duplicates found in {}", schemaToType.keySet());
            copy = new IdentityHashMap<>(schemaToType);
        }

        this.schemaToType = copy;

        // Two-phase indexing of choice/case nodes. First we load all choices. Note we are using typeToSchema argument,
        // not field, so as not to instantiate its entrySet.
        final Set<GeneratedType> choiceTypes = typeToSchema.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof ChoiceEffectiveStatement)
            .map(entry -> {
                final Type key = entry.getKey();
                verify(key instanceof GeneratedType, "Unexpected choice type %s", key);
                return (GeneratedType) key;
            })
            .collect(Collectors.toUnmodifiableSet());

        final Multimap<Type, Type> builder = MultimapBuilder.hashKeys(choiceTypes.size()).arrayListValues().build();
        for (Entry<Type, WithStatus> entry : typeToSchema.entrySet()) {
            if (entry.getValue() instanceof CaseEffectiveStatement) {
                final Type type = entry.getKey();
                verify(type instanceof GeneratedType, "Unexpected case type %s", type);
                builder.put(verifyNotNull(implementedChoiceType(((GeneratedType) type).getImplements(), choiceTypes),
                    "Cannot determine choice type for %s", type), type);
            }
        }

        choiceToCases = ImmutableMultimap.copyOf(builder);
    }

    private static GeneratedType implementedChoiceType(final List<Type> impls, final Set<GeneratedType> choiceTypes) {
        for (Type impl : impls) {
            if (impl instanceof GeneratedType && choiceTypes.contains(impl)) {
                return (GeneratedType) impl;
            }
        }
        return null;
    }

    public BindingRuntimeTypes(final EffectiveModelContext schemaContext,
            final Map<Type, AugmentationSchemaNode> typeToAugmentation,
            final BiMap<Type, WithStatus> typeToDefiningSchema, final Map<QName, Type> identities) {
        this(schemaContext, typeToAugmentation, typeToDefiningSchema, typeToDefiningSchema.inverse(), identities);
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
        return Optional.ofNullable(schemaToType.get(schema));
    }

    public Optional<Type> findOriginalAugmentationType(final AugmentationSchemaNode augment) {
        // If the augment statement does not contain any child nodes, we did not generate an augmentation, as it would
        // be plain littering.
        // FIXME: MDSAL-695: this check is rather costly (involves filtering), can we just rely on the not being found
        //                   in the end? all we are saving is essentially two map lookups after all...
        if (augment.getChildNodes().isEmpty()) {
            return Optional.empty();
        }

        // FIXME: MDSAL-695: We should have enough information from mdsal-binding-generator to receive a (sparse) Map
        //                   for current -> original lookup. When combined with schemaToType, this amounts to the
        //                   inverse view of what 'typeToSchema' holds
        AugmentationSchemaNode current = augment;
        while (true) {
            // If this augmentation has been added through 'uses foo { augment bar { ... } }', we need to invert that
            // walk and arrive at the original declaration site, as that is where we generated 'grouping foo's
            // augmentation. That site may have a different module, hence the augment namespace may be different.
            final Optional<AugmentationSchemaNode> original = current.getOriginalDefinition();
            if (original.isEmpty()) {
                return findType(current);
            }
            current = original.orElseThrow();
        }
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
