/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.tailf.common.parser;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.input.InputStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.output.OutputStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

@Beta
public final class TailFActionRFC6020StatementSupport extends AbstractTailFActionStatementSupport {
    private static final TailFActionRFC6020StatementSupport INSTANCE = new TailFActionRFC6020StatementSupport();

    private TailFActionRFC6020StatementSupport() {
        super();
    }

    public static TailFActionRFC6020StatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected StatementSupport<?, ?, ?> inputStatementSupport() {
        return InputStatementRFC6020Support.getInstance();
    }

    @Override
    protected StatementSupport<?, ?, ?> outputStatementSupport() {
        return OutputStatementRFC6020Support.getInstance();
    }
}
