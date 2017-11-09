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
    // TODO: share instances
    private static final StatementSupport<?, ?, ?> IMPLICIT_INPUT = new InputStatementRFC7950Support();
    private static final StatementSupport<?, ?, ?> IMPLICIT_OUTPUT = new OutputStatementRFC7950Support();

    @Override
    StatementSupport<?, ?, ?> implictInput() {
        return IMPLICIT_INPUT;
    }

    @Override
    StatementSupport<?, ?, ?> implictOutput() {
        return IMPLICIT_OUTPUT;
    }
}
