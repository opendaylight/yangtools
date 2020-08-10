/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

final class Token44444 extends AbstractSourceToken {
    private final int type;
    private final int line;
    private final int charPositionInLine;
    private final int startIndex;
    private final int stopIndex;

    Token44444(final Pair<TokenSource, CharStream> source, final int type, final int line, final int charPositionInLine,
            final int startIndex, final int stopIndex) {
        super(source);
        this.type = type;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }
}
