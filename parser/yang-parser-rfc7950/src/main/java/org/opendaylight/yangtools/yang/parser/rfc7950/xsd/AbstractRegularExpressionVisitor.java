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
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.COMMA;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.CatEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.Char;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.ComplEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.DASH;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.EndCategory;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.EndCharGroup;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.EndQuantity;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.IsBlock;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.IsCategory;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.LPAREN;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.MultiCharEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.NegCharGroup;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.NestedCatEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.NestedComplEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.NestedMultiCharEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.NestedNegCharGroup;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.NestedPosCharGroup;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.NestedSingleCharEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.PIPE;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.PLUS;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.PosCharGroup;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.QUESTION;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.QuantExact;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.RPAREN;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.STAR;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.SingleCharEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.StartQuantity;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.WildcardEsc;

import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.BranchContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassEscContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassExprContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.PieceContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.PosCharGroupContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.RegExpContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.RootContext;

@NonNullByDefault
abstract class AbstractRegularExpressionVisitor {

    final void visitRoot(final RootContext ctx) {
        visitRegExp(child(ctx, 0, RegExpContext.class));
    }

    private void visitRegExp(final RegExpContext ctx) {
        final var size = ctx.getChildCount();
        if (size == 0) {
            throw unexpectedShape(ctx);
        }

        startExpression((size + 1) / 2);

        final var it = ctx.children.iterator();
        visitBranch(next(it, BranchContext.class));
        while (it.hasNext()) {
            verifyToken(next(it, TerminalNode.class), PIPE);
            visitBranch(next(it, BranchContext.class));
        }

        endExpression();
    }

    abstract void startExpression(int expectedBranchCount);

    abstract void endExpression();

    private void visitBranch(final BranchContext ctx) {
        final var size = ctx.getChildCount();
        if (size == 0) {
            emptyBranch();
            return;
        }

        startBranch(size);

        for (var child : ctx.children) {
            if (child instanceof PieceContext piece) {
                pieces.add(parsePiece(piece));
            } else {
                throw unexpectedChild(child);
            }
        }

        endBranch();
    }

    abstract void emptyBranch();

    abstract void startBranch(int size);

    abstract void endBranch();

    private void visitPiece(final PieceContext ctx) {
        final var first = ctx.children.getFirst();
        switch (first) {
            case CharClassEscContext esc -> {
                visitCharClassEsc(esc);
                visitQuantifier(ctx, 1);
            }
            case CharClassExprContext expr -> {
                visitCharClassExpr(expr);
                visitQuantifier(ctx, 1);
            }
            case TerminalNode terminal -> {
                switch (terminal.getSymbol().getType()) {
                    case Char -> {
                        startPieceChar(terminal.getText());
                        visitQuantifier(ctx, 1);
                    }
                    case WildcardEsc -> {
                        startPieceWilcardEsc();
                        visitQuantifier(ctx, 1);
                    }
                    case LPAREN -> {
                        childToken(ctx, 2, RPAREN);
                        new ParenRegularExpression(parseRegExp(child(ctx, 1, RegExpContext.class)));
                        visitQuantifier(ctx, 3);
                    }
                    default -> throw unexpectedToken(terminal);
                }
            }
            default -> throw unexpectedChild(first);
        }
    }

    private void visitCharClassEsc(final CharClassEscContext esc) {
        final var terminal = verifySingleToken(esc);
        switch (terminal.getType()) {
            case SingleCharEsc, NestedSingleCharEsc -> startPieceSingleCharEsc(terminal.getText());
            case MultiCharEsc, NestedMultiCharEsc -> startPieceMultiCharEsc(terminal.getText());
            case CatEsc, NestedCatEsc -> {
                startPieceCategory(false);
                visitCharacterProperty(esc);
            }
            case ComplEsc, NestedComplEsc -> {
                startPieceCategory(true);
                visitCharacterProperty(esc);
            }
            default -> throw unexpectedToken(terminal);
        }
    }

    private void visitCharacterProperty(final CharClassEscContext ctx) {
        final var terminal = childToken(verifyChildCount(ctx, 3), 1);
        childToken(ctx, 2, EndCategory);
        switch (terminal.getType()) {
            case IsBlock -> endPieceCategoryIsBlock(terminal.getText());
            case IsCategory -> endPieceCategoryIsCategory(terminal.getText());
            default -> throw unexpectedToken(terminal);
        }
    }

    private void visitCharClassExpr(final CharClassExprContext expr) {
        final var first = verifyChildTerminal(expr.children.getFirst());
        final int size = expr.getChildCount();
        childToken(expr, size - 1, EndCharGroup);

        startPieceCharClassExpr(switch (first.getType()) {
            case NegCharGroup, NestedNegCharGroup -> true;
            case PosCharGroup, NestedPosCharGroup -> false;
            default -> throw unexpectedToken(first);
        });

        /**
         * The raison d'être for this class: this method disambiguates the innards of a character class expression's
         * {@code charGroup}.
         */
        final var second = expr.children.get(1);
        switch (second) {
            case PosCharGroupContext posCharGroup -> {
                final var firstGroup = parsePosCharGroup(posCharGroup);
                switch (size) {
                        case 3 -> firstGroup;
                        case 4 -> {
                            childToken(expr, 2, DASH);
                            yield union(firstGroup, SimpleCharacterGroup.DASH);
                        }
                        case 5 -> {
                            childToken(expr, 2, DASH);
                            yield new DifferenceCharacterGroup(firstGroup,
                                parseCharClassExpr(child(expr, 3, CharClassExprContext.class)));
                        }
                        case 6 -> {
                            childToken(expr, 2, DASH);
                            childToken(expr, 3, DASH);
                            yield new DifferenceCharacterGroup(union(firstGroup, SimpleCharacterGroup.DASH),
                                parseCharClassExpr(child(expr, 4, CharClassExprContext.class)));
                        }
                        default -> throw unexpectedShape(expr);
                    }
            }
            case TerminalNode terminal -> {
                verifyToken(terminal, DASH);
                switch(size) {
                        case 3 -> SimpleCharacterGroup.DASH;
                        case 5 -> {
                            childToken(expr, 2, DASH);
                            yield new DifferenceCharacterGroup(SimpleCharacterGroup.DASH,
                                parseCharClassExpr(child(expr, 3, CharClassExprContext.class)));
                        }
                        default -> throw unexpectedShape(expr);
                    }
            }
            default -> throw unexpectedChild(second);
        }
    }

    private static CharacterGroup parsePosCharGroup(final PosCharGroupContext ctx) {
        final var size = ctx.getChildCount();
        if (size == 0) {
            throw unexpectedShape(ctx);
        }

        final var components = new ArrayList<CharacterGroup>(size);
        final var children = ctx.children;

        // first child could be a DASH
        int offset;
        if (children.getFirst() instanceof TerminalNode terminal && terminal.getSymbol().getType() == DASH) {
            components.add(SimpleCharacterGroup.DASH);
            offset = 1;
        } else {
            offset = 0;
        }

        fillPosCharGroup(components, children, offset, size);

        // TODO: merge spans of SimpleCharacterGroups
        return components.size() == 1 ? components.getFirst() : new UnionCharacterGroup(components);
    }



    private void visitQuantifier(final PieceContext ctx, final int offset) {
        final var size = ctx.getChildCount();
        if (size == offset) {
            endPiece();
            return;
        }

        final var first = verifyChildTerminal(ctx.children.get(offset));
        endPiece(switch (first.getType()) {
            case PLUS -> Plus.INSTANCE;
            case QUESTION -> Question.INSTANCE;
            case STAR -> Star.INSTANCE;
            case StartQuantity -> parseQuantity(ctx, offset + 1, size);
            default -> throw unexpectedToken(first);
        });
    }

    abstract void startPieceChar(String character);

    abstract void startPieceSingleCharEsc(String text);

    abstract void startPieceMultiCharEsc(String text);

    abstract void startPieceWilcardEsc();

    abstract void startPieceCategory(boolean complement);

    abstract void endPieceCategoryIsBlock(String blockName);

    abstract void endPieceCategoryIsCategory(String literal);

    abstract void startPieceCharClassExpr(boolean complement);

    abstract void endPieceCharClassExpr();

    abstract void endPiece();

    abstract void endPiece(Quantifier quantifier);

    private static Quantifier parseQuantity(final PieceContext ctx, final int offset, final int size) {
        childToken(ctx, size - 1, EndQuantity);
        final var first = childQuantExact(ctx, offset);
        final var remaining = size - offset;
        if (remaining == 2) {
            return new QuantExact(first);
        }
        childToken(ctx, offset + 1, COMMA);
        return switch (remaining) {
            case 3 -> new QuantMin(first);
            case 4 -> new QuantRange(first, childQuantExact(ctx, offset + 2));
            default -> throw unexpectedShape(ctx);
        };
    }


    private static <T extends ParseTree> T next(final Iterator<ParseTree> it, final Class<T> expected) {
        return expected.cast(it.next());
    }

    static final <T extends ParseTree> T child(final ParserRuleContext ctx, final int index, final Class<T> expected) {
        return expected.cast(verifyNotNull(ctx.children).get(index));
    }

    private static String childQuantExact(final ParserRuleContext ctx, final int index) {
        return childToken(ctx, index, QuantExact).getText();
    }

    private static Token childToken(final ParserRuleContext ctx, final int index, final int token) {
        return verifyToken(childToken(verifyNotNull(ctx.children), index), token);
    }

    private static Token childToken(final List<ParseTree> children, final int index) {
        return verifyChildTerminal(children.get(index));
    }

    private static ParseTree verifySingleChild(final ParserRuleContext ctx) {
        return verifyChildCount(ctx, 1).getFirst();
    }

    private static Token verifySingleToken(final ParserRuleContext ctx) {
        return verifyChildTerminal(verifySingleChild(ctx));
    }

    private static List<ParseTree> verifyChildCount(final ParserRuleContext ctx, final int expected) {
        if (ctx.getChildCount() != expected) {
            throw unexpectedShape(ctx);
        }
        return ctx.children;
    }

    private static Token verifyChildTerminal(final ParseTree ctx) {
        if (ctx instanceof TerminalNode terminal) {
            return terminal.getSymbol();
        }
        throw unexpectedChild(ctx);
    }

    static final Token verifyToken(final TerminalNode node, final int type) {
        return verifyToken(node.getSymbol(), type);
    }

    static final Token verifyToken(final Token token, final int expected) {
        final int type = token.getType();
        verify(type == expected, "Unexpected token %s when expecting %s", type, expected);
        return token;
    }

    static final VerifyException unexpectedChild(final ParseTree ctx) {
        return new VerifyException("Unexpected child " + ctx);
    }

    static final VerifyException unexpectedShape(final ParserRuleContext ctx) {
        return new VerifyException("Unexpected shape of " + ctx);
    }

    static final VerifyException unexpectedToken(final TerminalNode ctx) {
        return unexpectedToken(ctx.getSymbol());
    }

    static final VerifyException unexpectedToken(final Token ctx) {
        return new VerifyException("Unexpected token " + ctx);
    }
}
