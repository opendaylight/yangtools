/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Generator corresponding to a {@code rpc} statement.
 */
final class RpcGenerator extends CompositeSchemaTreeGenerator<RpcEffectiveStatement, RpcGenerator> {
    RpcGenerator(final RpcEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
    }

    @Override
    void pushToInference(final SchemaInferenceStack dataTree) {
        dataTree.enterSchemaTree(statement().argument());
    }

    @Override
    // FIXME: switch to the same thing we are using for 'action'
    ClassPlacement classPlacement() {
        return ClassPlacement.PHANTOM;
    }

    @Override
    GeneratedType createTypeImpl(final TypeBuilderFactory builderFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    void addAsGetterMethod(final GeneratedTypeBuilderBase<?> builder, final TypeBuilderFactory builderFactory) {
        // RPCs are a separate concept
    }
}
