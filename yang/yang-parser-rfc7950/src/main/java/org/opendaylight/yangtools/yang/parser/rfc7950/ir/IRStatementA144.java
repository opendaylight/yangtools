/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

final class IRStatementA144 extends IRStatementA1 {
    private final int startLine;
    private final int startColumn;

    IRStatementA144(final IRKeyword keyword, final IRArgument argument, final IRStatement statement,
            final int startLine, final int startColumn) {
        super(keyword, argument, statement);
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
