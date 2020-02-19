/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;

/**
 * The result of BindingGenerator run. Contains mapping between Types and SchemaNodes.
 */
@Beta
public final class BindingRuntimeTypes implements SchemaContextProvider, Immutable {
    private final @NonNull SchemaContext schemaContext;
    private final ImmutableMap<Type, AugmentationSchemaNode> typeToAugmentation;
    private final ImmutableBiMap<Type, WithStatus> typeToSchema;
    private final ImmutableMultimap<Type, Type> choiceToCases;
    private final ImmutableMap<QName, Type> identities;

    public BindingRuntimeTypes(final SchemaContext schemaContext,
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
    public SchemaContext getSchemaContext() {
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
}
