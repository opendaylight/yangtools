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
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code leaf-list} statement.
 */
public non-sealed interface LeafListStatement extends DataDefinitionStatement, ConfigStatement.OptionalIn<QName>,
        DefaultStatement.MultipleIn<QName>, MaxElementsStatement.OptionalIn<QName>,
        MinElementsStatement.OptionalIn<QName>, MustStatement.MultipleIn<QName>, OrderedByStatement.OptionalIn<QName>,
        TypeStatement.OptionalIn<QName>, UnitsStatement.OptionalIn<QName> {
    /**
     * The definition of {@code leaf-list} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull LeafListStatement, @NonNull LeafListEffectiveStatement> DEF =
        StatementDefinition.of(LeafListStatement.class, LeafListEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "leaf-list", "name");

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }
}
