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
 * Declared representation of a {@code choice} statement.
 */
public interface ChoiceStatement extends DataDefinitionStatement, ConfigStatement.OptionalIn<QName>,
        DefaultStatement.OptionalIn<QName>, MandatoryStatement.OptionalIn<QName> {
    /**
     * The definition of {@code choice} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull ChoiceStatement, @NonNull ChoiceEffectiveStatement> DEF =
        StatementDefinition.of(ChoiceStatement.class, ChoiceEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "choice", "name");

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }

    // FIXME: document
    default @NonNull Collection<? extends @NonNull CaseStatement> getCases() {
        return declaredSubstatements(CaseStatement.class);
    }
}
