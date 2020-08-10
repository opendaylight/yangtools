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

final class Token12122 extends AbstractLazyToken {
    private final byte type;
    private final short line;
    private final byte charPositionInLine;
    private final short startIndex;
    private final short stopIndex;

    Token12122(final Pair<TokenSource, CharStream> source, final int type, final int line, final int charPositionInLine,
            final int startIndex, final int stopIndex) {
        super(source);
        this.type = (byte) type;
        this.line = (short) line;
        this.charPositionInLine = (byte) charPositionInLine;
        this.startIndex = (short) startIndex;
        this.stopIndex = (short) stopIndex;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getLine() {
        return Short.toUnsignedInt(line);
    }

    @Override
    public int getCharPositionInLine() {
        return Byte.toUnsignedInt(charPositionInLine);
    }

    @Override
    public int getStartIndex() {
        return Short.toUnsignedInt(startIndex);
    }

    @Override
    public int getStopIndex() {
        return Short.toUnsignedInt(stopIndex);
    }
}
