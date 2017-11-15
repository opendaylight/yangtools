/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.rpc;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input.InputStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output.OutputStatementRFC7950Support;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

/**
 * Bridge class for RFC7950 RPCs. Specializes implicit input/output statements.
 *
 * @author Robert Varga
 */
@Beta
public final class RpcStatementRFC7950Support extends AbstractRpcStatementSupport {
    private static final RpcStatementRFC7950Support INSTANCE = new RpcStatementRFC7950Support();

    private RpcStatementRFC7950Support() {
        // Hidden
    }

    public static RpcStatementRFC7950Support getInstance() {
        return INSTANCE;
    }

    @Override
    StatementSupport<?, ?, ?> implictInput() {
        return InputStatementRFC7950Support.getInstance();
    }

    @Override
    StatementSupport<?, ?, ?> implictOutput() {
        return OutputStatementRFC7950Support.getInstance();
    }
}
