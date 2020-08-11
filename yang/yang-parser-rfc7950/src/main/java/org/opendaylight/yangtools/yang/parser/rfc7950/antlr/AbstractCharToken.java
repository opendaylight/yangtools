/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

abstract class AbstractCharToken extends AbstractTextToken {
    @Override
    public final int getStartIndex() {
        return -1;
    }

    @Override
    public final int getStopIndex() {
        return -1;
    }

    static final int value31(final int line, final int charPositionInLine) {
        return line << 8 | charPositionInLine & 0xFF;
    }

    static final int getLine(final short line) {
        return Short.toUnsignedInt(line);
    }

    static final int getLine(final int value31) {
        return value31 >>> 8;
    }

    static final int getCharPositionInLine(final short charPositionInLine) {
        return Short.toUnsignedInt(charPositionInLine);
    }

    static final int getCharPositionInLine(final int value31) {
        return value31 & 0xFF;
    }
}
