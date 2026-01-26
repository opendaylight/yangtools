/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code grouping} statement.
 */
public interface GroupingStatement extends DeclaredStatement<QName>, DataDefinitionStatement.MultipleIn<QName>,
        ActionStatement.MultipleIn<QName>, DescriptionStatement.OptionalIn<QName>, GroupingStatementMultipleIn<QName>,
        NotificationStatement.MultipleIn<QName>, ReferenceStatement.OptionalIn<QName>,
        StatusStatement.OptionalIn<QName>, TypedefStatement.MultipleIn<QName> {
    /**
     * The definition of {@code grouping} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull GroupingStatement, @NonNull GroupingEffectiveStatement> DEF =
        StatementDefinition.of(GroupingStatement.class, GroupingEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "grouping", "name");

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }
}
