/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

abstract sealed class AbstractInvokableGenerator<
        S extends SchemaTreeEffectiveStatement<?>,
        R extends CompositeRuntimeType> extends CompositeSchemaTreeGenerator<S, R>
        permits RpcGenerator, ActionGenerator {
    @NonNullByDefault
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
        return createTypeImpl(builderFactory,
            getChild(InputEffectiveStatement.class).getGeneratedType(builderFactory),
            getChild(OutputEffectiveStatement.class).getGeneratedType(builderFactory));
    }

    @NonNullByDefault
    abstract GeneratedType createTypeImpl(TypeBuilderFactory builderFactory, GeneratedType input, GeneratedType output);

    @NonNullByDefault
    private <T extends EffectiveStatement<?, ?>> AbstractExplicitGenerator<T, ?> getChild(final Class<T> type) {
        for (var child : this) {
            if (child instanceof AbstractExplicitGenerator<?, ?> explicit && type.isInstance(explicit.statement())) {
                @SuppressWarnings("unchecked")
                final var ret = (AbstractExplicitGenerator<T, ?>) explicit.getOriginal();
                return ret;
            }
        }
        throw new IllegalStateException("Cannot find " + type + " in " + this);
    }
}
