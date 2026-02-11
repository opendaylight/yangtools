/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ValueRanges;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ConstraintMetaDefinitionMixin;

sealed interface LengthConstraintMixin
        extends LengthEffectiveStatement, ConstraintMetaDefinitionMixin<ValueRanges, @NonNull LengthStatement>,
                DocumentedNode.Mixin<LengthEffectiveStatement>
        permits EmptyLengthEffectiveStatement, RegularLengthEffectiveStatement {
    @Override
    default LengthEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    default ConstraintMetaDefinition asConstraint() {
        return this;
    }
}
