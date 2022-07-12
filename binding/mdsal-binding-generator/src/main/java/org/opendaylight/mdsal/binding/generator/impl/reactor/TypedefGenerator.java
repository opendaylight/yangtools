/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultTypedefRuntimeType;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.ri.TypeConstants;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.runtime.api.TypedefRuntimeType;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
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

    TypedefGenerator(final TypedefEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
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
            for (AbstractTypeObjectGenerator<?, ?> derived : derivedGenerators) {
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
    TypeDefinition<?> extractTypeDefinition() {
        return statement().getTypeDefinition();
    }

    @Override
    GeneratedTransferObject createDerivedType(final TypeBuilderFactory builderFactory,
            final GeneratedTransferObject baseType) {
        final GeneratedTOBuilder builder = builderFactory.newGeneratedTOBuilder(typeName());
        builder.setTypedef(true);
        builder.setExtendsType(baseType);
        builder.setIsUnion(baseType.isUnionType());
        builder.setRestrictions(computeRestrictions());
        YangSourceDefinition.of(currentModule().statement(), statement()).ifPresent(builder::setYangSourceDefinition);

        final TypeDefinition<?> typedef = statement().getTypeDefinition();
        annotateDeprecatedIfNecessary(typedef, builder);
        addStringRegExAsConstant(builder, resolveRegExpressions(typedef));
        addUnits(builder, typedef);

        if (typedef instanceof BitsTypeDefinition bits) {
            addValidBits(builder, bits, baseType);
        }

        makeSerializable(builder);
        return builder.build();
    }

    private static void addValidBits(final GeneratedTOBuilder builder, final BitsTypeDefinition typedef,
            final GeneratedTransferObject baseType) {
        final var baseDef = verifyNotNull(baseBitsDefinition(baseType), "Could not find definition in %s", baseType);
        final var myBits = typedef.getBits();
        if (myBits.size() != baseDef.getBits().size()) {
            builder.addConstant(Types.immutableSetTypeFor(Types.STRING), TypeConstants.VALID_NAMES_NAME, typedef);
        }
    }

    private static BitsTypeDefinition baseBitsDefinition(final GeneratedTransferObject gto) {
        var wlk = gto;
        while (wlk != null) {
            for (var constant : wlk.getConstantDefinitions()) {
                if (TypeConstants.VALID_NAMES_NAME.equals(constant.getName())) {
                    return (BitsTypeDefinition) constant.getValue();
                }
            }

            wlk = wlk.getSuperType();
        }
        return null;
    }

    @Override
    TypedefRuntimeType createExternalRuntimeType(final Type type) {
        verify(type instanceof GeneratedType, "Unexpected type %s", type);
        return new DefaultTypedefRuntimeType((GeneratedType) type, statement());
    }

    @Override
    TypedefRuntimeType createInternalRuntimeType(final AugmentResolver resolver,
            final TypedefEffectiveStatement statement, final Type type) {
        // 'typedef' statements are not schema tree statements, they should not have internal references
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // typedefs are a separate concept
    }
}
