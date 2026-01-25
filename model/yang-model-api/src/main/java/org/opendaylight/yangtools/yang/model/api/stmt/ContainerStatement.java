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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code container} statement.
 */
public interface ContainerStatement extends DataDefinitionStatement, DataDefinitionAwareDeclaredStatement<QName>,
        ActionStatement.MultipleIn<QName>, ConfigStatement.OptionalIn<QName>, GroupingStatementMultipleIn<QName>,
        MustStatement.MultipleIn<QName>, NotificationStatement.MultipleIn<QName>, TypedefStatement.MultipleIn<QName> {
    /**
     * The definition of {@code container} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull ContainerStatement, @NonNull ContainerEffectiveStatement> DEF =
        StatementDefinition.of(ContainerStatement.class, ContainerEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "container", "name");

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }

    // FIXME: rename/document
    default @Nullable PresenceStatement getPresence() {
        final var opt = findFirstDeclaredSubstatement(PresenceStatement.class);
        return opt.isPresent() ? opt.orElseThrow() : null;
    }
}
