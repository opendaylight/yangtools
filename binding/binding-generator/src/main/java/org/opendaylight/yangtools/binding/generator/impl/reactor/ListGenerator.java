/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultListRuntimeType;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature.ValueMechanics;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code list} statement.
 */
sealed class ListGenerator extends CompositeSchemaTreeGenerator<ListEffectiveStatement, ListRuntimeType>
        permits EntryObjectGenerator {
    @NonNullByDefault
    ListGenerator(final ListEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final StatementNamespace namespace() {
        return StatementNamespace.LIST;
    }

    @Nullable KeyGenerator keyGenerator() {
        return null;
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterDataTree(statement().argument());
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newGeneratedTypeBuilder(typeName());
        addImplementsChildOf(builder);
        addUsesInterfaces(builder, builderFactory);
        addConcreteInterfaceMethods(builder);

        final var module = currentModule();
        module.addQNameConstant(builder, localName());

        final var keyGenerator = keyGenerator();
        if (keyGenerator != null) {
            // Add yang.binding.Identifiable and its key() method
            final var keyType = keyGenerator.getGeneratedType(builderFactory);
            builder.addImplementsType(BindingTypes.entryObject(builder.typeRef(), keyType));
            builder.addMethod(Naming.KEY_AWARE_KEY_NAME)
                .setReturnType(keyType)
                .addAnnotation(OVERRIDE_ANNOTATION);
        } else {
            addAugmentable(builder);
        }

        addGetterMethods(builder, builderFactory);

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(module, statement(), builder);
        builder.setModuleName(module.statement().argument().getLocalName());

        return builder.build();
    }

    @Override
    final Type methodReturnType(final TypeBuilderFactory builderFactory) {
        final var generatedType = super.methodReturnType(builderFactory);
        // We are wrapping the generated type in either a List or a Map based on presence of the key
        final var keyGenerator = keyGenerator();
        if (keyGenerator != null && statement().effectiveOrdering() == Ordering.SYSTEM) {
            return Types.mapTypeFor(keyGenerator.getGeneratedType(builderFactory), generatedType);
        }

        return Types.listTypeFor(generatedType);
    }

    @Override
    final MethodSignatureBuilder constructGetter(final GeneratedTypeBuilderBase<?> builder, final Type returnType) {
        final var ret = super.constructGetter(builder, returnType).setMechanics(ValueMechanics.NULLIFY_EMPTY);

        final var nonnull = builder
            .addMethod(Naming.getNonnullMethodName(localName().getLocalName()))
            .setReturnType(returnType)
            .setDefault(true);
        annotateDeprecatedIfNecessary(nonnull);

        return ret;
    }

    @Override
    final CompositeRuntimeTypeBuilder<ListEffectiveStatement, ListRuntimeType> createBuilder(
            final ListEffectiveStatement statement) {
        return new CompositeRuntimeTypeBuilder<>(statement) {
            @Override
            ListRuntimeType build(final GeneratedType type, final ListEffectiveStatement statement,
                    final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
                final var keyGenerator = keyGenerator();
                return new DefaultListRuntimeType(type, statement, children, augments,
                    // FIXME: the key here is not rebased correctly :(
                    keyGenerator != null ? keyGenerator.getRuntimeType() : null);
            }
        };
    }
}
