/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

public interface PatternStatement extends ConstrainedDocumentedDeclaredStatement<PatternConstraint> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.PATTERN;
    }

    default @NonNull PatternConstraint getValue() {
        // FIXME: YANGTOOLS-908: verifyNotNull() should not be needed here
        return verifyNotNull(argument());
    }

    /**
     * Return a modifier statement, if present. In RFC6020 semantics, there are no modifiers and this methods always
     * returns null.
     *
     * @return modifier statement, nul if not present.
     */
    default @Nullable ModifierStatement getModifierStatement() {
        final Optional<ModifierStatement> opt = findFirstDeclaredSubstatement(ModifierStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
