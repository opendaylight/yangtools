/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRStatement;

final class IRStatementContext extends StatementContext {
    private final IRStatement rootStatement;

    IRStatementContext(final IRStatement rootStatement) {
        super(null, 0);
        this.rootStatement = requireNonNull(rootStatement);
        // FIXME: initialize state
    }
}
