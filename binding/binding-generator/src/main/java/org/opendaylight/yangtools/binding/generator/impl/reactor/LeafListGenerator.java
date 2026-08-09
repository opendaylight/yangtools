/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.binding.generator.impl.rt.DefaultLeafListRuntimeType;
import org.opendaylight.yangtools.binding.model.api.ReturnTypeCompat;
import org.opendaylight.yangtools.binding.model.api.SystemLeafList;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.UserLeafList;
import org.opendaylight.yangtools.binding.runtime.api.LeafListRuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;

/**
 * Generator corresponding to a {@code leaf-list} statement.
 */
final class LeafListGenerator
        extends AbstractTypeAwareGenerator<LeafListEffectiveStatement, LeafListRuntimeType, LeafListGenerator> {
    @NonNullByDefault
    LeafListGenerator(final LeafListEffectiveStatement statement, final DataContainerGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    StatementNamespace namespace() {
        return StatementNamespace.LEAF_LIST;
    }

    @Override
    ReturnTypeCompat methodReturnType() {
        // If we are a leafref and the reference cannot be resolved, we need to generate a list wildcard, not
        // List<Object>, we will try to narrow the return type in subclasses.
        final var type = super.methodReturnType();
        return switch (statement().effectiveOrdering()) {
            case SYSTEM -> SystemLeafList.of(type);
            case USER -> UserLeafList.of(type);
        };
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
