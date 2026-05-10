/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultFeatureRuntimeType;
import org.opendaylight.yangtools.binding.model.api.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.runtime.api.FeatureRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

final class FeatureGenerator extends AbstractExplicitGenerator<FeatureEffectiveStatement, FeatureRuntimeType> {
    @NonNullByDefault
    FeatureGenerator(final FeatureEffectiveStatement statement, final ModuleGenerator parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.FEATURE;
    }

    @Override
    void pushToInference(final SchemaInferenceStack inferenceStack) {
        throw new UnsupportedOperationException("Cannot push " + statement() + " to data tree");
    }

    @Override
    FeatureRuntimeType createExternalRuntimeType(final Type type) {
        if (type instanceof FeatureArchetype archetype) {
            return new DefaultFeatureRuntimeType(archetype);
        }
        throw new VerifyException("Unexpected type " + type);
    }

    @Override
    FeatureRuntimeType createInternalRuntimeType(final AugmentResolver resolver,
            final FeatureEffectiveStatement statement, final Type type) {
        // 'feature' statements are not part of schema tree and hence should never an internal reference
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    FeatureArchetype createTypeImpl(final TypeBuilderFactory builderFactory) {
        return new FeatureArchetype(typeName(), statement());
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // features are a separate concept
    }
}
