/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

sealed class IRStatement044 extends IRStatement permits IRStatement144, IRStatementL44 {
    private final int startLine;
    private final int startColumn;

    IRStatement044(final IRKeyword keyword, final IRArgument argument, final int startLine, final int startColumn) {
        super(keyword, argument);
        this.startLine = startLine;
        this.startColumn = startColumn;
    }

    @Override
    public final int startLine() {
        return startLine;
    }

    @Override
    public final int startColumn() {
        return startColumn;
    }
}
