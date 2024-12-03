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
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.Char;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.DASH;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.EndCategory;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.EndCharGroup;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.IsBlock;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.LPAREN;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.MultiCharEsc;
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.NegCharGroup;
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
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.AtomContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.BranchContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CatEscContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassEscContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassExprContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharGroupContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharPropContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.ComplEscContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.PieceContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.PosCharGroupContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.QuantMinContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.QuantRangeContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.QuantifierContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.QuantityContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.RegExpContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.RootContext;
import org.opendaylight.yangtools.yang.parser.rfc7950.antlr.SourceExceptionParser;

@NonNullByDefault
public record RegularExpression(List<Branch> branches) implements PatternFragment {
    public RegularExpression {
        branches = List.copyOf(branches);
        if (branches.isEmpty()) {
            throw new IllegalArgumentException("empty branches");
        }
    }

    public static RegularExpression parse(final StatementSourceReference ref, final String str) {
        final var lexer = new regexLexer(CharStreams.fromString(str));
        final var parser = new regexParser(new CommonTokenStream(lexer));
        return parseRoot(SourceExceptionParser.parse(lexer, parser, parser::root, ref));
    }

    @Override
    public void appendPatternFragment(final StringBuilder sb) {
        final var it = branches.iterator();
        it.next().appendPatternFragment(sb);
        it.forEachRemaining(branch -> branch.appendPatternFragment(sb.append('|')));
    }

    private static RegularExpression parseRoot(final RootContext ctx) {
        return parseRegExp(child(ctx, 0, RegExpContext.class));
    }

    private static RegularExpression parseRegExp(final RegExpContext ctx) {
        final var size = ctx.children.size();
        verify(size != 0, "No children in %s", ctx);

        final var branches = new ArrayList<Branch>((size + 1) / 2);
        final var it = ctx.children.iterator();
        while (it.hasNext()) {
            branches.add(parseBranch(next(it, BranchContext.class)));
            next(it, PIPE);
        }

        return new RegularExpression(branches);
    }

    private static Branch parseBranch(final BranchContext ctx) {
        final var size = ctx.getChildCount();
        if (size == 0) {
            return Branch.EMPTY;
        }

        final var pieces = new ArrayList<Piece>(size);
        for (var child : ctx.children) {
            if (child instanceof PieceContext piece) {
                pieces.add(parsePiece(piece));
            } else {
                throw new VerifyException("Unexpected child " + child);
            }
        }
        return new Branch(pieces);
    }

    private static Piece parsePiece(final PieceContext ctx) {
        final var atom = parseAtom(child(ctx, 0, AtomContext.class));
        return switch (ctx.getChildCount()) {
            case 1 -> new Piece(atom, null);
            case 2 -> new Piece(atom, parseQuantifier(child(ctx, 1, QuantifierContext.class)));
            default -> throw new VerifyException("Unexpected shape of " + ctx);
        };
    }

    private static Atom parseAtom(final AtomContext ctx) {
        return switch (ctx.getChildCount()) {
            case 1 -> {
                final var first = ctx.children.getFirst();
                yield switch (first) {
                    case CharClassContext charClass -> parseCharClass(charClass);
                    case TerminalNode terminal -> new NormalCharacter(verifyToken(terminal, Char).getText());
                    default -> throw new VerifyException("Unexpected child " + first);
                };
            }
            case 3 -> {
                child(ctx, 0, LPAREN);
                child(ctx, 2, RPAREN);
                yield new ParenRegularExpression(parseRegExp(child(ctx, 1, RegExpContext.class)));
            }
            default -> throw new VerifyException("Unexpected shape of " + ctx);
        };
    }

    private static CharacterClass parseCharClass(final CharClassContext ctx) {
        final var first = verifySingleChild(ctx);
        return switch (first) {
            case CharClassEscContext esc -> parseCharClassEscape(esc);
            case CharClassExprContext expr -> parseCharClassExpr(expr);
            case TerminalNode terminal -> {
                verifyToken(terminal, WildcardEsc);
                yield Dot.INSTANCE;
            }
            default -> throw new VerifyException("Unexpected child " + first);
        };
    }

    private static CharacterClass parseCharClassEscape(final CharClassEscContext ctx) {
        final var first = verifySingleChild(ctx);
        return switch (first) {
            case TerminalNode terminal -> switch (terminal.getSymbol().getType()) {
                case SingleCharEsc, NestedSingleCharEsc ->
                    SingleCharacterEscape.ofLiteral(terminal.getText());
                case MultiCharEsc, NestedMultiCharEsc ->
                    MultiCharacterEscape.ofLiteral(terminal.getText());
                default -> throw new VerifyException("Unexpected token " + terminal);
            };
            case CatEscContext category -> parseCategoryEscape(category);
            case ComplEscContext complement -> parseComplementEscape(complement);
            default -> throw new VerifyException("Unexpected child " + first);
        };
    }

    private static CategoryEscape parseCategoryEscape(final CatEscContext ctx) {
        verifyChildCount(ctx, 3);
        child(ctx, 2, EndCategory);
        return new CategoryEscape(parseCharacterProperty(child(ctx, 1, CharPropContext.class)));
    }

    private static ComplementEscape parseComplementEscape(final ComplEscContext ctx) {
        verifyChildCount(ctx, 3);
        child(ctx, 2, EndCategory);
        return new ComplementEscape(parseCharacterProperty(child(ctx, 1, CharPropContext.class)));
    }

    private static CharacterProperty parseCharacterProperty(final CharPropContext ctx) {
        final var first = verifySingleChild(ctx);
        if (first instanceof TerminalNode terminal) {
            return switch (terminal.getSymbol().getType()) {
                case IsBlock -> new IsBlock(terminal.getText().substring(2));
                case regexLexer.IsCategory -> IsCategory.ofLiteral(terminal.getText());
                default -> throw new VerifyException("Unexpected token " + terminal);
            };
        }

        throw new VerifyException("Unexpected child " + first);
    }

    private static CharacterClassExpression parseCharClassExpr(final CharClassExprContext ctx) {
        final var header = child(verifyChildCount(ctx, 3), 0);
        child(ctx, 2, EndCharGroup);
        final var charGroup = parseCharGroup(child(ctx, 1, CharGroupContext.class));
        return switch (header.getType()) {
            case NegCharGroup, NestedNegCharGroup -> new CharacterClassExpression.Negative(charGroup);
            case PosCharGroup, NestedPosCharGroup -> new CharacterClassExpression.Positive(charGroup);
            default -> throw new VerifyException("Unexpected token " + header);
        };
    }

    private static CharacterGroup parseCharGroup(final CharGroupContext ctx) {
        final var first = child(ctx.children, 0);
        return switch (first) {
            case PosCharGroupContext posCharGroup -> switch(ctx.getChildCount()) {
                case 1 -> {
//                  posCharGroup

                }
                case 2 -> {
//                  posCharGroup DASH

                }
                case 3 -> {
//                  posCharGroup DASH charClassExpr

                }
                case 4 -> {
//                  posCharGroup DASH DASH charClassExpr

                }
                default -> throw new VerifyException("Unexpected shape of " + ctx);
            };
            case TerminalNode terminal -> {
                verifyToken(terminal, DASH);
                yield switch(ctx.getChildCount()) {
                    case 1 -> SimpleCharacterGroup.DASH;
                    case 3 -> {
                        child(ctx, 1, DASH);
                        yield new SubtractionCharacterGroup(SimpleCharacterGroup.DASH,
                            parseCharClassExpr(child(ctx, 2, CharClassExprContext.class)));
                    }
                    default -> throw new VerifyException("Unexpected shape of " + ctx);
                };
            }
            default -> throw new VerifyException("Unexpected child " + first);
        };
    }

    private static Quantifier parseQuantifier(final QuantifierContext ctx) {
        return switch (ctx.getChildCount()) {
            case 1 -> {
                final var first = ctx.children.getFirst();
                yield switch (first) {
                    case TerminalNode terminal -> {
                        yield switch (terminal.getSymbol().getType()) {
                            case PLUS -> Plus.INSTANCE;
                            case QUESTION -> Question.INSTANCE;
                            case STAR -> Star.INSTANCE;
                            default -> throw new VerifyException("Unexpected token " + terminal);
                        };
                    }
                    default -> throw new VerifyException("Unexpected child " + first);
                };
            }
            case 3 -> {
                child(ctx, 0, StartQuantity);
                child(ctx, 2, StartQuantity);
                yield parseQuantity(child(ctx, 1, QuantityContext.class));
            }
            default -> throw new VerifyException("Unexpected shape of " + ctx);
        };
    }

    private static Quantifier parseQuantity(final QuantityContext ctx) {
        final var first = verifySingleChild(ctx);
        return switch (first) {
            case QuantMinContext min -> parseQuantMin(min);
            case QuantRangeContext range -> parseQuantRange(range);
            case TerminalNode terminal -> new QuantExact(verifyQuantExact(terminal));
            default -> throw new VerifyException("Unexpected child " + first);
        };
    }

    private static QuantMin parseQuantMin(final QuantMinContext ctx) {
        verifyChildCount(ctx, 2);
        child(ctx, 1, COMMA);
        return new QuantMin(childQuantExact(ctx, 0));
    }

    private static QuantRange parseQuantRange(final QuantRangeContext ctx) {
        verifyChildCount(ctx, 3);
        child(ctx, 1, COMMA);
        return new QuantRange(childQuantExact(ctx, 0), childQuantExact(ctx, 2));
    }

    private static <T extends ParseTree> T child(final ParserRuleContext ctx, final int index,
            final Class<T> expected) {
        return child(verifyNotNull(ctx.children), index, expected);
    }

    private static <T extends ParseTree> T child(final List<ParseTree> children, final int index,
            final Class<T> expected) {
        return expected.cast(children.get(index));
    }

    private static Token child(final ParserRuleContext ctx, final int index, final int token) {
        return verifyToken(child(ctx.children, index), token);
    }

    private static Token child(final List<ParseTree> children, final int index) {
        final var child = children.get(index);
        if (child instanceof TerminalNode terminal) {
            return terminal.getSymbol();
        }
        throw new VerifyException("Unexpected child " + child);
    }

    private static String childQuantExact(final ParserRuleContext ctx, final int index) {
        return child(ctx, index, QuantExact).getText();
    }

    private static <T extends ParseTree> T next(final Iterator<ParseTree> it, final Class<T> expected) {
        return expected.cast(it.next());
    }

    private static Token next(final Iterator<ParseTree> it, final int token) {
        return verifyToken(next(it, TerminalNode.class), token);
    }

    private static ParseTree verifySingleChild(final ParserRuleContext ctx) {
        return verifyChildCount(ctx, 1).getFirst();
    }

    private static List<ParseTree> verifyChildCount(final ParserRuleContext ctx, final int expected) {
        verify(ctx.getChildCount() == expected, "Unexpected shape of %s", ctx);
        return ctx.children;
    }

    private static String verifyQuantExact(final TerminalNode ctx) {
        return verifyToken(ctx, QuantExact).getText();
    }

    private static Token verifyToken(final TerminalNode node, final int type) {
        return verifyToken(node.getSymbol(), type);
    }

    private static Token verifyToken(final Token token, final int expected) {
        final int type = token.getType();
        verify(type == expected, "Unexpected token %s when expecting %s", type, expected);
        return token;
    }
}
