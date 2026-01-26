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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Declared representation of a {@code rpc} statement.
 */
public non-sealed interface RpcStatement extends DeclaredOperationStatement {
    /**
     * A {@link DeclaredStatement} that is a parent of multiple {@link RpcStatement}s.
     * @param <A> Argument type ({@link Empty} if statement does not have argument.)
     */
    @Beta
    interface MultipleIn<A> extends DeclaredStatement<A> {
        /**
         * {@return all {@code RpcStatement} substatements}
         */
        default @NonNull Collection<? extends @NonNull RpcStatement> rpcStatements() {
            return declaredSubstatements(RpcStatement.class);
        }
    }

    /**
     * The definition of {@code rpc} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<QName, @NonNull RpcStatement, @NonNull RpcEffectiveStatement> DEF =
        StatementDefinition.of(RpcStatement.class, RpcEffectiveStatement.class,
            YangConstants.RFC6020_YIN_MODULE, "rpc", YangArgumentDefinitions.NAME_AS_QNAME);

    @Override
    default StatementDefinition<QName, ?, ?> statementDefinition() {
        return DEF;
    }
}
