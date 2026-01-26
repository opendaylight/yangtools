/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

public interface RpcContextReferenceStatement extends UnknownStatement<String> {
    /**
     * The definition of {@code yang-ext:rpc-context-reference} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull RpcContextReferenceStatement,
        @NonNull RpcContextReferenceEffectiveStatement> DEF = StatementDefinition.of(
            RpcContextReferenceStatement.class, RpcContextReferenceEffectiveStatement.class,
            OpenDaylightExtensionsConstants.ORIGINAL_MODULE, "rpc-context-reference",
            ArgumentDefinition.of(String.class, OpenDaylightExtensionsConstants.ORIGINAL_MODULE, "context-type"));

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
