/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.RpcStatementImpl;

/**
 * Bridge class for RFC7950 RPCs. Specializes implicit input/output statements.
 *
 * @author Robert Varga
 */
@Beta
public final class RpcStatementRfc7950Support extends RpcStatementImpl.Definition {
    // TODO: share instances
    private static final StatementSupport<?, ?, ?> IMPLICIT_INPUT = new InputStatementRfc7950Support();
    private static final StatementSupport<?, ?, ?> IMPLICIT_OUTPUT = new OutputStatementRfc7950Support();

    @Override
    protected StatementSupport<?, ?, ?> implictInput() {
        return IMPLICIT_INPUT;
    }

    @Override
    protected StatementSupport<?, ?, ?> implictOutput() {
        return IMPLICIT_OUTPUT;
    }
}
