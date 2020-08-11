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

/**
 * Intermediate general token implementation. This cuts {@code channel}, {@code text} fields completely, as we typically
 * we do not use them. {@code type} and {@code charPositionInLine} are cut down to a single byte, and {@code line} is
 * cut down to an unsigned short. {@code startIndex} and {@code stopIndex} are kept at full four-byte range, this making
 * this implementation useful beyond 64K-char file mark.
 *
 * <p>
 * This class ends up costing 24/40/32/32 bytes instead of 48/64/48/48 bytes, a saving of 33-50% in the same scenarios
 * as {@link Token12122} across all possible file sizes.
 */
final class Token12144 extends AbstractSourceToken {
    private final byte type;
    private final short line;
    private final byte charPositionInLine;

    private final int startIndex;
    private final int stopIndex;

    Token12144(final Pair<TokenSource, CharStream> source, final int type, final int line, final int charPositionInLine,
            final int startIndex, final int stopIndex) {
        super(source);
        this.type = (byte) type;
        this.line = (short) line;
        this.charPositionInLine = (byte) charPositionInLine;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
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
        return startIndex;
    }

    @Override
    public int getStopIndex() {
        return stopIndex;
    }
}
