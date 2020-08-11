/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

final class LeftBraceToken22 extends AbstractLeftBraceToken {
    private final short line;
    private final short charPositionInLine;

    LeftBraceToken22(final int line, final int charPositionInLine) {
        this.line = (short) line;
        this.charPositionInLine = (short) charPositionInLine;
    }

    @Override
    public int getLine() {
        return getLine(line);
    }

    @Override
    public int getCharPositionInLine() {
        return getCharPositionInLine(charPositionInLine);
    }
}
