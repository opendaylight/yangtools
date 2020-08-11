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
import org.antlr.v4.runtime.Token;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementLexer;

final class SingletonCharToken extends AbstractCharToken {
    static final @NonNull SingletonCharToken COLON = new SingletonCharToken(YangStatementLexer.COLON, ":");
    static final @NonNull SingletonCharToken SEMICOLON = new SingletonCharToken(YangStatementLexer.SEMICOLON, ";");
    static final @NonNull SingletonCharToken LEFT_BRACE = new SingletonCharToken(YangStatementLexer.LEFT_BRACE, "{");
    static final @NonNull SingletonCharToken RIGHT_BRACE = new SingletonCharToken(YangStatementLexer.RIGHT_BRACE, "}");
    static final @NonNull SingletonCharToken PLUS = new SingletonCharToken(YangStatementLexer.PLUS, "+");
    static final @NonNull SingletonCharToken SEP = new SingletonCharToken(YangStatementLexer.SEP, " ");

    private final String text;
    private final int type;

    private SingletonCharToken(final int type, final String text) {
        this.type = type;
        this.text = requireNonNull(text);
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
    public Token asSingleton() {
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).add("text", text).toString();
    }
}
