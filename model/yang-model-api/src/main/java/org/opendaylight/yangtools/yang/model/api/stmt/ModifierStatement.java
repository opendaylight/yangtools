/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;

/**
 * Represents YANG modifier statement.
 *
 * <p>The "modifier" statement, which is an optional substatement to the "pattern" statement, takes as an argument
 * the string "invert-match". If a pattern has the "invert-match" modifier present, the type is restricted to values
 * that do not match the pattern.
 */
public interface ModifierStatement extends DeclaredStatement<ModifierKind> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.MODIFIER;
    }
}
