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
    // TODO: share instances
    private static final StatementSupport<?, ?, ?> IMPLICIT_INPUT = new InputStatementRFC6020Support();
    private static final StatementSupport<?, ?, ?> IMPLICIT_OUTPUT = new OutputStatementRFC6020Support();

    @Override
    StatementSupport<?, ?, ?> implictInput() {
        return IMPLICIT_INPUT;
    }

    @Override
    StatementSupport<?, ?, ?> implictOutput() {
        return IMPLICIT_OUTPUT;
    }
}