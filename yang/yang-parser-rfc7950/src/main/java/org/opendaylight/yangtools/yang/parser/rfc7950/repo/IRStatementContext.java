/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser;
import org.opendaylight.yangtools.yang.parser.antlr.YangStatementParser.StatementContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.ExplicitTextToken;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.Concatenation;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRArgument.Single;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRKeyword;
import org.opendaylight.yangtools.yang.parser.rfc7950.ir.IRStatement;

final class IRStatementContext extends StatementContext {
    private static final TerminalNode COLON =
            new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.COLON, ":"));
    private static final TerminalNode SEMICOLON =
            new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.SEMICOLON, ";"));
    private static final TerminalNode LBRACE =
            new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.LEFT_BRACE, "{"));
    private static final TerminalNode RBRACE =
            new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.RIGHT_BRACE, "}"));
    private static final TerminalNode PLUS =
            new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.PLUS, "+"));
    private static final TerminalNode SEP =
            new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.SEP, " "));
    private static final TerminalNode DQUOT_END =
            new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.DQUOT_END, "\""));
    private static final TerminalNode SQUOT_END =
            new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.SQUOT_END, "'"));

    IRStatementContext(final IRStatement rootStatement) {
        super(null, -1);
        initialize(rootStatement);
    }

    private void initialize(final IRStatement stmt) {
        final List<ParseTree> tmp = new ArrayList<>();
        final IRKeyword keyword = stmt.keyword();
        final String prefix = keyword.prefix();
        if (prefix != null) {
            start = new ExplicitTextToken(YangStatementParser.IDENTIFIER, prefix);
            tmp.add(new TerminalNodeImpl(start));
            tmp.add(COLON);
            tmp.add(new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.IDENTIFIER,
                keyword.identifier())));
        } else {
            start = new ExplicitTextToken(YangStatementParser.IDENTIFIER, keyword.identifier());
            tmp.add(new TerminalNodeImpl(start));
        }

        final IRArgument argument = stmt.argument();
        if (argument != null) {
            tmp.add(SEP);
            addArgumentChildren(tmp, stmt.argument());
        }

        final List<? extends IRStatement> stmts = stmt.statements();
        if (!stmts.isEmpty()) {
            tmp.add(LBRACE);
            for (IRStatement child : stmt.statements()) {
                tmp.add(new IRStatementContext(child));
            }
            tmp.add(RBRACE);
        } else {
            tmp.add(SEMICOLON);
        }

        this.children = ImmutableList.copyOf(tmp);
    }

    private static void addArgumentChildren(final List<ParseTree> children, final IRArgument argument) {
        if (argument instanceof Concatenation) {
            final Iterator<? extends Single> it = ((Concatenation) argument).parts().iterator();
            addArgumentChildren(children, it.next());
            while (it.hasNext()) {
                children.add(PLUS);
                addArgumentChildren(children, it.next());
            }
        } else if (argument instanceof Single) {
            addArgumentChildren(children, (Single) argument);
        } else {
            throw new IllegalStateException("Unhandled argument " + argument);
        }
    }

    private static void addArgumentChildren(final List<ParseTree> children, final Single argument) {
        final String str = argument.string();
        if (argument.isValidIdentifier()) {
            children.add(new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.IDENTIFIER, str)));
        } else if (argument.needQuoteCheck()) {
            children.add(new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.UQUOT_STRING, str)));
        } else if (argument.needUnescape()) {
            children.add(new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.DQUOT_STRING, str)));
            children.add(DQUOT_END);
        } else {
            children.add(new TerminalNodeImpl(new ExplicitTextToken(YangStatementParser.SQUOT_STRING, str)));
            children.add(SQUOT_END);
        }
    }
}
