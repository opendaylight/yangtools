/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Version-specific support for determining the {@link AugmentStrategy} for a particular statement.
 */
enum AugmentStrategyResolver {
    /**
     * Companion to {@link AugmentStatementRFC6020Support}.
     */
    RFC6020() {
        @Override
        AugmentStrategy strategyFor(
                final StmtContext<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> stmt) {
            return AugmentStrategy.rfc6020();
        }
    },
    /**
     * Companion to {@link AugmentStatementRFC7950Support}.
     */
    RFC7950() {
        @Override
        AugmentStrategy strategyFor(
                final StmtContext<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> stmt) {
            // RFC7950, page 120:
            //    If the augmentation adds mandatory nodes (see Section 3) that
            //    represent configuration to a target node in another module, the
            //    augmentation MUST be made conditional with a "when" statement.
            return stmt.hasSubstatement(WhenEffectiveStatement.class) ? AugmentStrategy.conditional()
                : AugmentStrategy.unconditional();
        }
    };

    /**
     * {@return the {@link AugmentStrategy} to use with specified statement}
     * @param stmt the statement
     */
    abstract @NonNull AugmentStrategy strategyFor(
        @NonNull StmtContext<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> stmt);
}
