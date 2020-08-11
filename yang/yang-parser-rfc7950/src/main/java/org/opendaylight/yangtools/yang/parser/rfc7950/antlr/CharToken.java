/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementLexer;

final class CharToken extends AbstractTextToken {
    static final CharToken COLON = new CharToken(YangStatementLexer.COLON, ":");
    static final CharToken SEMICOLON = new CharToken(YangStatementLexer.SEMICOLON, ";");
    static final CharToken LEFT_BRACE = new CharToken(YangStatementLexer.LEFT_BRACE, "{");
    static final CharToken RIGHT_BRACE = new CharToken(YangStatementLexer.RIGHT_BRACE, "}");
    static final CharToken PLUS = new CharToken(YangStatementLexer.PLUS, "+");
    static final CharToken SEP = new CharToken(YangStatementLexer.SEP, " ");

    private final String text;
    private final int type;

    private CharToken(final int type, final String text) {
        this.type = type;
        this.text = requireNonNull(text);
    }

    @Override
    public int getStartIndex() {
        return -1;
    }

    @Override
    public int getStopIndex() {
        return -1;
    }

    @Override
    public int getLine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCharPositionInLine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).add("text", text).toString();
    }
}
