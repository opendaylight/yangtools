/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.antlr;

import static java.util.Objects.requireNonNull;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

abstract class AbstractSourceToken extends AbstractToken {
    private final Pair<TokenSource, CharStream> source;

    AbstractSourceToken(final Pair<TokenSource, CharStream> source) {
        this.source = requireNonNull(source);
    }

    @Override
    public final TokenSource getTokenSource() {
        return source.a;
    }

    @Override
    public final CharStream getInputStream() {
        return source.b;
    }
}
