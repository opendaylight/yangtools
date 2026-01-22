/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code leaf-list} statement.
 */
public interface LeafListStatement
    extends MultipleElementsDeclaredStatement, TypeAwareDeclaredStatement<QName>,
            ConfigStatementAwareDeclaredStatement<QName>, MustStatementAwareDeclaredStatement<QName> {
    /**
     * The definition of {@code action} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition DEFINITION = StatementDefinition.of(
        LeafListStatement.class, LeafListEffectiveStatement.class,
        YangConstants.RFC6020_YIN_MODULE, "leaf-list", "name");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }

    /**
     * Return default statements defined in this leaf-list. For RFC6020 semantics, this method returns an empty
     * collection.
     *
     * @return collection of default statements
     */
    default @NonNull Collection<? extends @NonNull DefaultStatement> getDefaults() {
        return declaredSubstatements(DefaultStatement.class);
    }
}
