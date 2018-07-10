/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.regex;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.AtomContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.BranchContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.PieceContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.QuantifierContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.RegExpContext;

/**
 * XSD regular expression.
 */
@Beta
public final class XSDRegex {
    private final RegExpContext exp;

    private XSDRegex(final RegExpContext exp) {
        this.exp = requireNonNull(exp);
    }

    public static XSDRegex parse(final String regex) {
        final regexLexer lexer = new regexLexer(CharStreams.fromString(regex));
        lexer.removeErrorListeners();

        final List<IllegalArgumentException> errors = new ArrayList<>();
        final ANTLRErrorListener listener = new BaseErrorListener() {
            @Override
            public void syntaxError(final @Nullable Recognizer<?, ?> recognizer, final @Nullable Object offendingSymbol,
                    final int line, final int charPositionInLine, final @Nullable String msg,
                    final @Nullable RecognitionException cause) {
                final IllegalArgumentException ex = new IllegalArgumentException(msg);
                ex.initCause(cause);
                if (errors.isEmpty()) {
                    errors.add(ex);
                } else {
                    errors.get(0).addSuppressed(ex);
                }
            }
        };
        lexer.addErrorListener(listener);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final regexParser parser = new regexParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        final RegExpContext exp = parser.regExp();
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }

        return new XSDRegex(exp);
    }

    public Pattern toJavaPattern() {
        final StringBuilder sb = new StringBuilder().append('^');

        final int size = exp.getChildCount();
        switch (size) {
            case 0:
                // No-op
                break;
            case 1:
                // Single branch: just append that
                addBranch(sb, exp, 0);
                break;
            default:
                // Multiple branches: enclose them in a non-capturing group
                sb.append("(?:");
                addBranch(sb, exp, 0);
                for (int i = 2; i < size; i += 2) {
                    sb.append('|');
                    addBranch(sb, exp, i);
                }
                sb.append(')');
        }

        final String regex = sb.append('$').toString();
        return Pattern.compile(regex);
    }

    private static void addBranch(final StringBuilder sb, final RegExpContext regExp, final int offset) {
        final BranchContext branch = getChild(regExp, offset, BranchContext.class);

        for (PieceContext piece : branch.piece()) {
            final AtomContext atom = piece.atom();
            switch (atom.getChildCount()) {
                case 1:
                    final ParseTree child = atom.getChild(0);
                    if (child instanceof TerminalNode) {
                        sb.append(getTerminal(atom, 0, regexParser.NormalChar));
                    } else {
                        throw new UnsupportedOperationException();
                    }
                    break;
                case 3:
                    addRegExp(sb, getChild(atom, 1, RegExpContext.class));
                    break;
                default:
                    throw new IllegalStateException("Unexpected atom " + atom);
            }

            final QuantifierContext quantifier = piece.quantifier();
            if (quantifier != null) {
                // FIXME: emit quantifier
            }
        }
    }

    private static void addRegExp(final StringBuilder sb, final RegExpContext regExp) {
        sb.append('(');

        // FIXME: add regExp

        sb.append(')');
    }

    private static <T> T getChild(final ParseTree tree, final int offset, final Class<T> type) {
        final ParseTree child = tree.getChild(offset);
        checkState(type.isInstance(child), "Unexpected child %s when expecting %s", child, type);
        return type.cast(child);
    }

    private static String getTerminal(final ParseTree tree, final int offset, final int type) {
        final ParseTree child = tree.getChild(offset);
        checkState(child instanceof TerminalNode, "Unexpected non-terminal %s", child);
        final TerminalNode terminal = (TerminalNode) child;
        checkState(terminal.getSymbol().getType() == type, "Unexpected terminal %s", terminal);
        return terminal.getText();
    }
}
