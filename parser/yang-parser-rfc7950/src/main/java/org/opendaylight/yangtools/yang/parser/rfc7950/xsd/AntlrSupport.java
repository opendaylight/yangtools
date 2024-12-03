/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.xsd;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import java.util.Iterator;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.antlr.regexLexer;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.RegExpContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.SourceExceptionParser;

@NonNullByDefault
final class AntlrSupport {
    private AntlrSupport() {
        // Hidden un purpose
    }

    static RegExpContext parseRegularExpression(final StatementSourceReference ref, final String str) {
        final var lexer = new regexLexer(CharStreams.fromString(str));
        final var parser = new regexParser(new CommonTokenStream(lexer));
        return child(SourceExceptionParser.parse(lexer, parser, parser::root, ref), 0, RegExpContext.class);
    }

    static <T extends ParseTree> T child(final ParserRuleContext ctx, final int index, final Class<T> expected) {
        return expected.cast(verifyNotNull(ctx.children).get(index));
    }

    static Token childToken(final ParserRuleContext ctx, final int index, final int token) {
        return verifyToken(childToken(verifyNotNull(ctx.children), index), token);
    }

    static Token childToken(final List<ParseTree> children, final int index) {
        return verifyChildTerminal(children.get(index));
    }

    static <T extends ParseTree> T next(final Iterator<ParseTree> it, final Class<T> expected) {
        return expected.cast(it.next());
    }

    static ParseTree verifySingleChild(final ParserRuleContext ctx) {
        return verifyChildCount(ctx, 1).getFirst();
    }

    static Token verifySingleToken(final ParserRuleContext ctx) {
        return verifyChildTerminal(verifySingleChild(ctx));
    }

    static List<ParseTree> verifyChildCount(final ParserRuleContext ctx, final int expected) {
        if (ctx.getChildCount() != expected) {
            throw unexpectedShape(ctx);
        }
        return ctx.children;
    }

    static Token verifyChildTerminal(final ParseTree ctx) {
        if (ctx instanceof TerminalNode terminal) {
            return terminal.getSymbol();
        }
        throw unexpectedChild(ctx);
    }

    static Token verifyToken(final TerminalNode node, final int type) {
        return verifyToken(node.getSymbol(), type);
    }

    static Token verifyToken(final Token token, final int expected) {
        final int type = token.getType();
        verify(type == expected, "Unexpected token %s when expecting %s", type, expected);
        return token;
    }

    static VerifyException unexpectedChild(final ParseTree ctx) {
        return new VerifyException("Unexpected child " + ctx);
    }

    static VerifyException unexpectedShape(final ParserRuleContext ctx) {
        return new VerifyException("Unexpected shape of " + ctx);
    }

    static VerifyException unexpectedToken(final TerminalNode ctx) {
        return unexpectedToken(ctx.getSymbol());
    }

    static VerifyException unexpectedToken(final Token ctx) {
        return new VerifyException("Unexpected token " + ctx);
    }
}
