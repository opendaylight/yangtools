/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code pattern} statement.
 */
public interface PatternStatement extends ConstrainedDocumentedDeclaredStatement<PatternExpression> {
    /**
     * The definition of {@code pattern} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(
        PatternStatement.class, PatternEffectiveStatement.class, YangConstants.RFC6020_YIN_MODULE, "pattern", "value");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }

    /**
     * Return a modifier statement, if present. In RFC6020 semantics, there are no modifiers and this methods always
     * returns null.
     *
     * @return modifier statement, null if not present.
     */
    default @Nullable ModifierStatement getModifierStatement() {
        final var opt = findFirstDeclaredSubstatement(ModifierStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }
}
