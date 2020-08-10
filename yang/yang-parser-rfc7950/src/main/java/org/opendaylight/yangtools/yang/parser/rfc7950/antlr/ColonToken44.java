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
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementLexer;

final class ColonToken44 extends AbstractCharToken44 {
    ColonToken44(final Pair<TokenSource, CharStream> source, final int line, final int charPositionInLine) {
        super(source, line, charPositionInLine);
    }

    @Override
    public int getType() {
        return YangStatementLexer.COLON;
    }

    @Override
    public String getText() {
        return ":";
    }
}
