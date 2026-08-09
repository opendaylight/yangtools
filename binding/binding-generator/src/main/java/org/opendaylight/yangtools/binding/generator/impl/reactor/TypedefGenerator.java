/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultTypedefRuntimeType;
import org.opendaylight.yangtools.binding.model.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.TypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.runtime.api.TypedefRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code typedef} statement.
 */
final class TypedefGenerator extends AbstractTypeObjectGenerator<TypedefEffectiveStatement, TypedefRuntimeType> {
    /**
     * List of all generators for types directly derived from this typedef. We populate this list during initial type
     * linking. It allows us to easily cascade inferences made by this typedef down the type derivation tree.
     */
    private List<AbstractTypeObjectGenerator<?, ?>> derivedGenerators = null;

    @NonNullByDefault
    TypedefGenerator(final TypedefEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.TYPEDEF;
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterTypedef(statement().argument());
    }

    void addDerivedGenerator(final AbstractTypeObjectGenerator<?, ?> derivedGenerator) {
        if (derivedGenerators == null) {
            derivedGenerators = new ArrayList<>(4);
        }
        derivedGenerators.add(requireNonNull(derivedGenerator));
    }

    @Override
    void bindDerivedGenerators(final TypeReference reference) {
        // Trigger any derived resolvers ...
        if (derivedGenerators != null) {
            for (var derived : derivedGenerators) {
                derived.bindTypeDefinition(reference);
            }
        }
        // ... and make sure nobody can come in late
        derivedGenerators = List.of();
    }

    @Override
    ClassPlacement classPlacementImpl() {
        return ClassPlacement.TOP_LEVEL;
    }

    @Override
    TypeObjectArchetype.OfClass<?> createDerivedType(final TypeObjectArchetype.OfClass<?> baseType) {
        final var typeName = typeName();
        final var statement = statement();
        final var typedef = statement.typeDefinition();

        return switch (baseType) {
            case BitsTypeObjectArchetype bits ->
                BitsTypeObjectArchetype.of(typeName, statement, (BitsTypeDefinition) typedef, bits);
            case ScalarTypeObjectArchetype scalar ->
                ScalarTypeObjectArchetype.of(typeName, statement, typedef, scalar.valueType(),
                    Restrictions.compute(statement, statement.typeStatement()), scalar);
            case UnionTypeObjectArchetype union -> UnionTypeObjectArchetype.of(typeName, statement, union);
        };
    }

    @Override
    TypedefRuntimeType createExternalRuntimeType(final Type type) {
        return new DefaultTypedefRuntimeType(verifyGeneratedType(type), statement());
    }

    @Override
    TypedefRuntimeType createInternalRuntimeType(final AugmentResolver resolver,
            final TypedefEffectiveStatement statement, final Type type) {
        // 'typedef' statements are not schema tree statements, they should not have internal references
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    void addAsGetterMethod(final List<GetterMethod> list) {
        // typedefs are a separate concept
    }
}
