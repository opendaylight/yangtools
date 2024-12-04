/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.regex;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.RunAutomaton;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.regex.antlr.regexLexer;
import org.opendaylight.yangtools.regex.antlr.regexParser;
import org.opendaylight.yangtools.regex.antlr.regexParser.AtomContext;
import org.opendaylight.yangtools.regex.antlr.regexParser.BranchContext;
import org.opendaylight.yangtools.regex.antlr.regexParser.CharClassContext;
import org.opendaylight.yangtools.regex.antlr.regexParser.PieceContext;
import org.opendaylight.yangtools.regex.antlr.regexParser.QuantMinContext;
import org.opendaylight.yangtools.regex.antlr.regexParser.QuantRangeContext;
import org.opendaylight.yangtools.regex.antlr.regexParser.QuantifierContext;
import org.opendaylight.yangtools.regex.antlr.regexParser.QuantityContext;
import org.opendaylight.yangtools.regex.antlr.regexParser.RegExpContext;

/**
 * XML Schema Regular Expression, as defined in <a href="https://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#regexs">
 * XML Schema Part 2: Datatypes Second Edition</a>.
 */
@Beta
public final class RegEx implements Immutable {
    private final RunAutomaton automaton;

    private RegEx(final Automaton automaton) {
        this.automaton = new RunAutomaton(automaton, false);
    }

    public static @NonNull RegEx of(final String str) throws ParseException {
        final var lexer = new regexLexer(CharStreams.fromString(str));
        final var parser = new regexParser(new CommonTokenStream(lexer));

        // Disconnect from console and capture any errors encountered
        final var listener = new CapturingErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        final var antlr = parser.root().regExp();
        listener.reportError();
        return new RegEx(toAutomaton(antlr));
    }

    public boolean match(final String str) {
        return automaton.run(str);
    }

    private static Automaton toAutomaton(final RegExpContext regExp) {
        final var it = regExp.children.iterator();
        var aut = toAutomaton(next(it, BranchContext.class));
        while (it.hasNext()) {
            next(it, regexParser.PIPE);
            aut = aut.union(toAutomaton(next(it, BranchContext.class)));
        }

        aut.minimize();
        return aut;
    }

    private static Automaton toAutomaton(final BranchContext branch) {
        var ret = BasicAutomata.makeEmptyString();
        final var children = branch.children;
        if (children != null) {
            final var it = children.iterator();
            while (it.hasNext()) {
                ret = ret.concatenate(toAutomaton(next(it, PieceContext.class)));
            }
        }
        return ret;
    }

    private static Automaton toAutomaton(final PieceContext piece) {
        final var children = verifyNotNull(piece.children);
        final var atom = toAutomaton(child(children, 0, AtomContext.class));
        switch (children.size()) {
            case 1:
                return atom;
            case 2:
                return toAutomaton(atom, child(children, 1, QuantifierContext.class));
            default:
                throw new VerifyException("Unexpected children " + children);
        }
    }

    private static Automaton toAutomaton(final CharClassContext charClass) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    private static Automaton toAutomaton(final AtomContext atom) {
        final var children = verifyNotNull(atom.children);
        switch (children.size()) {
            case 1:
                final var child = children.get(0);
                if (child instanceof CharClassContext) {
                    return toAutomaton((CharClassContext) child);
                }
                verify(child instanceof TerminalNode, "Unexpected child %s", child);
                final var token = ((TerminalNode) child).getSymbol();
                verify(token.getType() == regexParser.Char, "Unexpected token %s", token);
                return Automaton.makeString(token.getText());
            case 3:
                return toAutomaton(child(children, 1, RegExpContext.class));
            default:
                throw new VerifyException("Unexpected children " + children);
        }
    }

    private static Automaton toAutomaton(final Automaton atom, final QuantifierContext quantifier) {
        final var children = verifyNotNull(quantifier.children);
        switch (children.size()) {
            case 1:
                final var token = child(children, 0);
                switch (token.getType()) {
                    case regexParser.QUESTION:
                        return atom.optional();
                    case regexParser.STAR:
                        return atom.repeat();
                    case regexParser.PLUS:
                        return atom.repeat(1);
                    default:
                        throw new VerifyException("Unexpected child " + token);
                }
            case 3:
                return toAutomaton(atom, child(children, 1, QuantityContext.class));
            default:
                throw new VerifyException("Unexpected children " + children);
        }
    }

    private static Automaton toAutomaton(final Automaton atom, final QuantityContext quantity) {
        final var children = verifyNotNull(quantity.children);
        verify(children.size() == 1, "Unexpected children %s", children);

        final var child = children.get(0);
        if (child instanceof TerminalNode) {
            final var quant = parseQuant(((TerminalNode) child).getSymbol());
            return atom.repeat(quant, quant);
        } else if (child instanceof QuantMinContext) {
            return atom.repeat(quantChild(((QuantMinContext) child).children, 0));
        } else if (child instanceof QuantRangeContext) {
            final var rangeChildren = ((QuantRangeContext) child).children;
            return atom.repeat(quantChild(rangeChildren, 0), quantChild(rangeChildren, 2));
        } else {
            throw new VerifyException("Unexpected child " + child);
        }
    }

    private static int parseQuant(final Token token) {
        verify(token.getType() == regexParser.QuantExact, "Unexpected child %s", token);
        return Integer.parseUnsignedInt(token.getText());
    }

    private static int quantChild(final List<ParseTree> children, final int index) {
        return parseQuant(child(children, index));
    }

    private static <T extends ParseTree> T child(final List<ParseTree> children, final int index,
            final Class<T> expected) {
        return expected.cast(children.get(index));
    }

    private static Token child(final List<ParseTree> children, final int index) {
        final var child = children.get(index);
        verify(child instanceof TerminalNode, "Unexpected child %s", child);
        return ((TerminalNode) child).getSymbol();
    }

    private static <T extends ParseTree> T next(final Iterator<ParseTree> it, final Class<T> expected) {
        return expected.cast(it.next());
    }

    private static Token next(final Iterator<ParseTree> it, final int token) {
        final var ret = next(it, TerminalNode.class).getSymbol();
        final int type = ret.getType();
        verify(type == token, "Unexpected token %s when expecting %s", type, token);
        return ret;
    }
}
