/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.mdsal.binding.generator.impl.rt.DefaultLeafListRuntimeType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.runtime.api.LeafListRuntimeType;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;

/**
 * Generator corresponding to a {@code leaf-list} statement.
 */
final class LeafListGenerator
        extends AbstractTypeAwareGenerator<LeafListEffectiveStatement, LeafListRuntimeType, LeafListGenerator> {
    LeafListGenerator(final LeafListEffectiveStatement statement, final AbstractCompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.LEAF_LIST;
    }

    @Override
    Type methodReturnType(final TypeBuilderFactory builderFactory) {
        // If we are a leafref and the reference cannot be resolved, we need to generate a list wildcard, not
        // List<Object>, we will try to narrow the return type in subclasses.
        final Type type = super.methodReturnType(builderFactory);
        final boolean isObject = Types.objectType().equals(type);

        if (statement().ordering() == Ordering.SYSTEM) {
            return isObject ? Types.setTypeWildcard() : Types.setTypeFor(type);
        }
        return isObject ? Types.listTypeWildcard() : Types.listTypeFor(type);
    }

    @Override
    LeafListRuntimeType createExternalRuntimeType(final Type type) {
        return new DefaultLeafListRuntimeType(type, statement());
    }

    @Override
    LeafListRuntimeType createInternalRuntimeType(final AugmentResolver resolver,
            final LeafListEffectiveStatement statement, final Type type) {
        return new DefaultLeafListRuntimeType(type, statement);
    }
}
