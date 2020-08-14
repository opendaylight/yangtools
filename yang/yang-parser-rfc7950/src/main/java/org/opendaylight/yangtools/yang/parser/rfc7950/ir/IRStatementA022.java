/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

final class IRStatementA022 extends IRStatementA0 {
    private final short startLine;
    private final short startColumn;

    IRStatementA022(final IRKeyword keyword, final IRArgument argument, final int startLine, final int startColumn) {
        super(keyword, argument);
        this.startLine = (short) startLine;
        this.startColumn = (short) startColumn;
    }

    @Override
    public int startLine() {
        return startLine(startLine);
    }

    @Override
    public int startColumn() {
        return startColumn(startColumn);
    }
}
