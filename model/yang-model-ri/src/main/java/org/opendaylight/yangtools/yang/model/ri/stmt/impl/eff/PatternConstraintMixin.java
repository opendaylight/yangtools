/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternExpression;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.ConstraintMetaDefinitionMixin;

sealed interface PatternConstraintMixin
        extends PatternConstraint, PatternEffectiveStatement,
            ConstraintMetaDefinitionMixin<PatternExpression, @NonNull PatternStatement>,
            DocumentedNode.Mixin<PatternEffectiveStatement>
        permits EmptyPatternEffectiveStatement, RegularPatternEffectiveStatement {
    @Override
    default PatternEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    default PatternConstraint asConstraint() {
        return this;
    }

    @Override
    default String getJavaPatternString() {
        return argument().getJavaPatternString();
    }

    @Override
    default String getRegularExpressionString() {
        return argument().getRegularExpressionString();
    }

    @Override
    default Optional<ModifierKind> getModifier() {
        return findFirstEffectiveSubstatementArgument(ModifierEffectiveStatement.class);
    }
}
