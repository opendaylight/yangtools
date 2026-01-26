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
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Declared representation of a {@code deviation} statement.
 */
public interface DeviationStatement extends DocumentedDeclaredStatement<Absolute> {
    /**
     * The definition of {@code deviation} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<Absolute, @NonNull DeviationStatement, @NonNull DeviationEffectiveStatement> DEF =
        StatementDefinition.of(DeviationStatement.class, DeviationEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "deviation",
            ArgumentDefinition.of(Absolute.class, YangConstants.RFC6020_YIN_MODULE, "target-node"));

    @Override
    default StatementDefinition<Absolute, ?, ?> statementDefinition() {
        return DEF;
    }

    // FIXME: 11.0.0: evaluate usefulness of this
    default @NonNull Collection<? extends @NonNull DeviateStatement> getDeviateStatements() {
        return declaredSubstatements(DeviateStatement.class);
    }
}
