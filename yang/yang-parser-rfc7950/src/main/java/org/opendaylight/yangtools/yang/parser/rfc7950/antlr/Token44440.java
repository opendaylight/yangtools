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

class Token44440 extends AbstractLazyToken {
    private final int type;
    private final int line;
    private final int charPositionInLine;
    private final int startIndex;

    Token44440(final Pair<TokenSource, CharStream> source, final int type, final int line, final int charPositionInLine,
            final int startIndex) {
        super(source);
        this.type = type;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.startIndex = startIndex;
    }

    @Override
    public final int getType() {
        return type;
    }

    @Override
    public final int getLine() {
        return line;
    }

    @Override
    public final int getCharPositionInLine() {
        return charPositionInLine;
    }

    @Override
    public final int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return startIndex;
    }
}
