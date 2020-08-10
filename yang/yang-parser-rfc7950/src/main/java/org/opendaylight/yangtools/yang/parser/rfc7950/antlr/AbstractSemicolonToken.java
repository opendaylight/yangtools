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

abstract class AbstractSemicolonToken extends AbstractCharToken {
    AbstractSemicolonToken(final Pair<TokenSource, CharStream> source) {
        super(source);
    }

    @Override
    public final int getType() {
        return YangStatementLexer.SEMICOLON;
    }

    @Override
    public final String getText() {
        return ";";
    }
}
