/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.generator.impl.reactor.CollisionDomain.Member;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

/**
 * Specialization for legacy RPC services.
 */
final class RpcContainerGenerator extends OperationContainerGenerator {
    private final @NonNull String suffix;

    RpcContainerGenerator(final InputEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        suffix = BindingMapping.RPC_INPUT_SUFFIX;
    }

    RpcContainerGenerator(final OutputEffectiveStatement statement, final AbstractCompositeGenerator<?> parent) {
        super(statement, parent);
        suffix = BindingMapping.RPC_OUTPUT_SUFFIX;
    }

    @Override
    CollisionDomain parentDomain() {
        return getParent().parentDomain();
    }

    @Override
    AbstractCompositeGenerator<?> getPackageParent() {
        return getParent().getParent();
    }

    @Override
    Member createMember(final CollisionDomain domain) {
        return domain.addSecondary(this, getParent().ensureMember(), suffix, statement().argument());
    }
}
