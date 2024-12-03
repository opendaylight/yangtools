/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.regex.antlr.regexLexer;
import org.opendaylight.yangtools.regex.antlr.regexParser;
import org.opendaylight.yangtools.regex.antlr.regexParser.RootContext;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.SourceExceptionParser;

@NonNullByDefault
final class AntlrSupport {
    private AntlrSupport() {
        // Hidden un purpose
    }

    static RootContext parseRegularExpression(final StatementSourceReference ref, final String str) {
        final var lexer = new regexLexer(CharStreams.fromString(str));
        final var parser = new regexParser(new CommonTokenStream(lexer));
        return SourceExceptionParser.parse(lexer, parser, parser::root, ref);
    }
}
