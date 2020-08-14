/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import com.google.common.collect.ImmutableList;

final class IRStatementAL44 extends IRStatementAL {
    private final int startLine;
    private final int startColumn;

    IRStatementAL44(final IRKeyword keyword, final IRArgument argument, final ImmutableList<IRStatement> statements,
            final int startLine, final int startColumn) {
        super(keyword, argument, statements);
        this.startLine = startLine;
        this.startColumn = startColumn;
    }

    @Override
    public int startLine() {
        return startLine;
    }

    @Override
    public int startColumn() {
        return startColumn;
    }
}
