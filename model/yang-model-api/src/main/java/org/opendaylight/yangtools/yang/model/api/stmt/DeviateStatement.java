/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code deviate} statement.
 */
public interface DeviateStatement extends DeclaredStatement<DeviateKind> {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link DeviateStatement}s.
     *
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     * @since 15.0.1
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code DeviateStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull DeviateStatement> deviateStatements() {
            return declaredSubstatements(DeviateStatement.class);
        }
    }

    /**
     * The definition of {@code deviate} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<DeviateKind, @NonNull DeviateStatement, @NonNull DeviateEffectiveStatement> DEF =
        StatementDefinition.of(DeviateStatement.class, DeviateEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "deviate",
            ArgumentDefinition.of(DeviateKind.class, YangConstants.RFC6020_YIN_MODULE, "value"));

    @Override
    default StatementDefinition<DeviateKind, ?, ?> statementDefinition() {
        return DEF;
    }
}
