/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.rpc;

import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input.InputStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output.OutputStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

public final class RpcStatementRFC6020Support extends AbstractRpcStatementSupport {
    private static final RpcStatementRFC6020Support INSTANCE = new RpcStatementRFC6020Support();

    private RpcStatementRFC6020Support() {
        // Hidden
    }

    public static RpcStatementRFC6020Support getInstance() {
        return INSTANCE;
    }

    @Override
    StatementSupport<?, ?, ?> implictInput() {
        return InputStatementRFC6020Support.getInstance();
    }

    @Override
    StatementSupport<?, ?, ?> implictOutput() {
        return OutputStatementRFC6020Support.getInstance();
    }
}