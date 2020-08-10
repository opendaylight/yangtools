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

final class LeftBraceToken31 extends AbstractLeftBraceToken {
    private final int value;

    LeftBraceToken31(final Pair<TokenSource, CharStream> source, final int line, final int charPositionInLine) {
        super(source);
        value = value31(line, charPositionInLine);
    }

    @Override
    public int getLine() {
        return getLine(value);
    }

    @Override
    public int getCharPositionInLine() {
        return getCharPositionInLine(value);
    }
}
