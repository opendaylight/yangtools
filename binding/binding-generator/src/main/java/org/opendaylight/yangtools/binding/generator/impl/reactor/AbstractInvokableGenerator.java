/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

abstract class AbstractInvokableGenerator<S extends SchemaTreeEffectiveStatement<?>, R extends CompositeRuntimeType>
        extends CompositeSchemaTreeGenerator<S, R> {
    private static final JavaTypeName FUNCTIONAL_INTERFACE_ANNOTATION = JavaTypeName.create(FunctionalInterface.class);

    AbstractInvokableGenerator(final S statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    final void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    @Override
    final void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // RPCs/Actions are a separate concept
    }

    @Override
    final GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        final var builder = builderFactory.newGeneratedTypeBuilder(typeName());
        final var inputType = getChild(InputEffectiveStatement.class).getOriginal().getGeneratedType(builderFactory);
        final var outputType = getChild(OutputEffectiveStatement.class).getOriginal().getGeneratedType(builderFactory);
        addImplementedType(builderFactory, builder, inputType, outputType);
        builder.addAnnotation(FUNCTIONAL_INTERFACE_ANNOTATION);
        defaultImplementedInterace(builder);

        addQNameConstant(builder, statement().argument());

        annotateDeprecatedIfNecessary(builder);
        builderFactory.addCodegenInformation(currentModule(), statement(), builder);

        return builder.build();
    }

    abstract void addImplementedType(TypeBuilderFactory builderFactory, GeneratedTypeBuilder builder,
        GeneratedType input, GeneratedType output);

    private <T extends EffectiveStatement<?, ?>> AbstractExplicitGenerator<T, ?> getChild(final Class<T> type) {
        for (var child : this) {
            if (child instanceof AbstractExplicitGenerator) {
                @SuppressWarnings("unchecked")
                final var explicit = (AbstractExplicitGenerator<T, ?>)child;
                if (type.isInstance(explicit.statement())) {
                    return explicit;
                }
            }
        }
        throw new IllegalStateException("Cannot find " + type + " in " + this);
    }
}
