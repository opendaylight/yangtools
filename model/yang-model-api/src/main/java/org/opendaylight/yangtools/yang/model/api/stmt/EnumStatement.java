/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code enum} statement.
 */
public interface EnumStatement extends DocumentedDeclaredStatement.WithStatus<String>,
        IfFeatureAwareDeclaredStatement<String> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangStmtMapping.ENUM;
    }

    default @Nullable ValueStatement getValue() {
        final var opt = findFirstDeclaredSubstatement(ValueStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }
}
