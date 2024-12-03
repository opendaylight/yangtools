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
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.IsBlock;
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
import static org.opendaylight.yangtools.yang.parser.antlr.regexLexer.XmlChar;

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
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassEscContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassExprContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharGroupContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharOrEscContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharRangeContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.PieceContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.PosCharGroupContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.QuantMinContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.QuantRangeContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.QuantifierContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.QuantityContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.RegExpContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.RootContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.SeRangeContext;
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
            verifyToken(next(it, TerminalNode.class), PIPE);
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
                throw unexpectedChild(child);
            }
        }
        return new Branch(pieces);
    }

    private static Piece parsePiece(final PieceContext ctx) {
        final var atom = parseAtom(child(ctx, 0, AtomContext.class));
        return switch (ctx.getChildCount()) {
            case 1 -> new Piece(atom, null);
            case 2 -> new Piece(atom, parseQuantifier(child(ctx, 1, QuantifierContext.class)));
            default -> throw unexpectedShape(ctx);
        };
    }

    private static Atom parseAtom(final AtomContext ctx) {
        return switch (ctx.getChildCount()) {
            case 1 -> {
                final var first = ctx.children.getFirst();
                yield switch (first) {
                        case CharClassContext charClass -> parseCharClass(charClass);
                        case TerminalNode terminal -> new NormalCharacter(verifyToken(terminal, Char).getText());
                        default -> throw unexpectedChild(first);
                    };
            }
            case 3 -> {
                childToken(ctx, 0, LPAREN);
                childToken(ctx, 2, RPAREN);
                yield new ParenRegularExpression(parseRegExp(child(ctx, 1, RegExpContext.class)));
            }
            default -> throw unexpectedShape(ctx);
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
            default -> throw unexpectedChild(first);
        };
    }

    private static CharacterClass parseCharClassEscape(final CharClassEscContext ctx) {
        final var terminal = verifySingleToken(ctx);
        return switch (terminal.getType()) {
            case SingleCharEsc, NestedSingleCharEsc -> SingleCharacterEscape.ofLiteral(terminal.getText());
            case MultiCharEsc, NestedMultiCharEsc -> MultiCharacterEscape.ofLiteral(terminal.getText());
            case CatEsc, NestedCatEsc -> new CategoryEscape(parseCharacterProperty(ctx));
            case ComplEsc, NestedComplEsc -> new ComplementEscape(parseCharacterProperty(ctx));
            default -> throw unexpectedToken(terminal);
        };
    }

    private static CharacterProperty parseCharacterProperty(final CharClassEscContext ctx) {
        final var terminal = childToken(verifyChildCount(ctx, 3), 1);
        childToken(ctx, 2, EndCategory);
        return switch (terminal.getType()) {
            case IsBlock -> new IsBlock(terminal.getText().substring(2));
            case regexLexer.IsCategory -> IsCategory.ofLiteral(terminal.getText());
            default -> throw unexpectedToken(terminal);
        };
    }

    private static CharacterClassExpression parseCharClassExpr(final CharClassExprContext ctx) {
        final var header = childToken(verifyChildCount(ctx, 3), 0);
        childToken(ctx, 2, EndCharGroup);
        final var charGroup = parseCharGroup(child(ctx, 1, CharGroupContext.class));
        return switch (header.getType()) {
            case NegCharGroup, NestedNegCharGroup -> new CharacterClassExpression.Negative(charGroup);
            case PosCharGroup, NestedPosCharGroup -> new CharacterClassExpression.Positive(charGroup);
            default -> throw unexpectedToken(header);
        };
    }

    private static CharacterGroup parseCharGroup(final CharGroupContext ctx) {
        final var first = ctx.children.getFirst();
        return switch (first) {
            case PosCharGroupContext posCharGroup -> {
                final var firstGroup = parsePosCharGroup(posCharGroup);
                yield switch (ctx.getChildCount()) {
                        case 1 -> firstGroup;
                        case 2 -> {
                            childToken(ctx, 1, DASH);
                            yield union(firstGroup, SimpleCharacterGroup.DASH);
                        }
                        case 3 -> {
                            childToken(ctx, 1, DASH);
                            yield new DifferenceCharacterGroup(firstGroup,
                                parseCharClassExpr(child(ctx, 2, CharClassExprContext.class)));
                        }
                        case 4 -> {
                            childToken(ctx, 1, DASH);
                            childToken(ctx, 2, DASH);
                            yield new DifferenceCharacterGroup(union(firstGroup, SimpleCharacterGroup.DASH),
                                parseCharClassExpr(child(ctx, 3, CharClassExprContext.class)));
                        }
                        default -> throw unexpectedShape(ctx);
                    };
            }
            case TerminalNode terminal -> {
                verifyToken(terminal, DASH);
                yield switch(ctx.getChildCount()) {
                        case 1 -> SimpleCharacterGroup.DASH;
                        case 3 -> {
                            childToken(ctx, 1, DASH);
                            yield new DifferenceCharacterGroup(SimpleCharacterGroup.DASH,
                                parseCharClassExpr(child(ctx, 2, CharClassExprContext.class)));
                        }
                        default -> throw unexpectedShape(ctx);
                    };
            }
            default -> throw unexpectedChild(first);
        };
    }

    private static CharacterGroup parsePosCharGroup(final PosCharGroupContext ctx) {
        final var size = ctx.getChildCount();
        return switch (size) {
            case 0 -> throw unexpectedShape(ctx);
            case 1 -> parsePosCharGroupItem(ctx.children.getFirst());
            default -> {
                final var components = new ArrayList<CharacterGroup>(size);
                final var it = ctx.children.iterator();

                // first child could be a DASH
                final var first = it.next();
                final CharacterGroup firstGroup;
                if (first instanceof TerminalNode terminal) {
                    if (terminal.getSymbol().getType() == DASH) {
                        firstGroup = SimpleCharacterGroup.DASH;
                    } else {
                        throw unexpectedToken(terminal);
                    }
                } else {
                    firstGroup = parsePosCharGroupItem(first);
                }
                components.add(firstGroup);

                while (it.hasNext()) {
                    components.add(parsePosCharGroupItem(it.next()));
                }

                // TODO: merge spans of SimpleCharacterGroups
                yield components.size() == 1 ? components.getFirst() : new UnionCharacterGroup(components);
            }
        };
    }

    private static CharacterGroup parsePosCharGroupItem(final ParseTree ctx) {
        return switch (ctx) {
            case CharClassEscContext charClassEsc -> parseCharClassEscape(charClassEsc);
            case CharRangeContext charRange -> parseCharRange(charRange);
            default -> throw unexpectedChild(ctx);
        };
    }

    private static CharacterGroup parseCharRange(final CharRangeContext ctx) {
        final var first = verifySingleChild(ctx);
        return switch (first) {
            case TerminalNode terminal -> new SimpleCharacterGroup(verifyToken(terminal, XmlChar).getText());
            case SeRangeContext range -> parseSeRange(range);
            default -> throw unexpectedChild(first);
        };
    }

    private static RangeCharacterGroup parseSeRange(final SeRangeContext ctx) {
        verifyChildCount(ctx, 3);
        childToken(ctx, 1, DASH);
        return new RangeCharacterGroup(parseCharOrEsc(child(ctx, 0, CharOrEscContext.class)),
            parseCharOrEsc(child(ctx, 2, CharOrEscContext.class)));
    }

    private static String parseCharOrEsc(final CharOrEscContext ctx) {
        final var terminal = verifySingleToken(ctx);
        return switch (terminal.getType()) {
            case SingleCharEsc, XmlChar -> terminal.getText();
            default -> throw unexpectedToken(terminal);
        };
    }

    private static Quantifier parseQuantifier(final QuantifierContext ctx) {
        return switch (ctx.getChildCount()) {
            case 1 -> {
                final var first = ctx.children.getFirst();
                yield switch (first) {
                        case TerminalNode terminal -> switch (terminal.getSymbol().getType()) {
                            case PLUS -> Plus.INSTANCE;
                            case QUESTION -> Question.INSTANCE;
                            case STAR -> Star.INSTANCE;
                            default -> throw unexpectedToken(terminal);
                        };
                        default -> throw unexpectedChild(first);
                    };
            }
            case 3 -> {
                childToken(ctx, 0, StartQuantity);
                childToken(ctx, 2, StartQuantity);
                yield parseQuantity(child(ctx, 1, QuantityContext.class));
            }
            default -> throw unexpectedShape(ctx);
        };
    }

    private static Quantifier parseQuantity(final QuantityContext ctx) {
        final var first = verifySingleChild(ctx);
        return switch (first) {
            case QuantMinContext min -> parseQuantMin(min);
            case QuantRangeContext range -> parseQuantRange(range);
            case TerminalNode terminal -> new QuantExact(verifyQuantExact(terminal));
            default -> throw unexpectedChild(first);
        };
    }

    private static QuantMin parseQuantMin(final QuantMinContext ctx) {
        verifyChildCount(ctx, 2);
        childToken(ctx, 1, COMMA);
        return new QuantMin(childQuantExact(ctx, 0));
    }

    private static QuantRange parseQuantRange(final QuantRangeContext ctx) {
        verifyChildCount(ctx, 3);
        childToken(ctx, 1, COMMA);
        return new QuantRange(childQuantExact(ctx, 0), childQuantExact(ctx, 2));
    }

    private static CharacterGroup union(final CharacterGroup first, final CharacterGroup second) {
        if (first instanceof UnionCharacterGroup(var components)) {
            return appendGroup(components, second);
        }
        if (second instanceof UnionCharacterGroup(var components)) {
            return prependGroup(components, first);
        }
        if (first instanceof SimpleCharacterGroup(var fstr) && second instanceof SimpleCharacterGroup(var sstr)) {
            return new SimpleCharacterGroup(fstr + sstr);
        }
        return new UnionCharacterGroup(List.of(first, second));
    }

    private static CharacterGroup appendGroup(final List<CharacterGroup> components, final CharacterGroup group) {
        final var tmp = new ArrayList<CharacterGroup>(components.size() + 1);
        tmp.addAll(components);
        // TODO: SimpleCharacterGroup merge if applicable
        tmp.add(group);
        return new UnionCharacterGroup(tmp);
    }

    private static CharacterGroup prependGroup(final List<CharacterGroup> components, final CharacterGroup group) {
        final var tmp = new ArrayList<CharacterGroup>(components.size() + 1);
        // TODO: SimpleCharacterGroup merge if applicable
        tmp.add(group);
        tmp.addAll(components);
        return new UnionCharacterGroup(tmp);
    }

    private static <T extends ParseTree> T child(final ParserRuleContext ctx, final int index,
            final Class<T> expected) {
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

    private static <T extends ParseTree> T next(final Iterator<ParseTree> it, final Class<T> expected) {
        return expected.cast(it.next());
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

    private static VerifyException unexpectedChild(final ParseTree ctx) {
        return new VerifyException("Unexpected child " + ctx);
    }

    private static VerifyException unexpectedShape(final ParserRuleContext ctx) {
        return new VerifyException("Unexpected shape of " + ctx);
    }

    private static VerifyException unexpectedToken(final TerminalNode ctx) {
        return unexpectedToken(ctx.getSymbol());
    }

    private static VerifyException unexpectedToken(final Token ctx) {
        return new VerifyException("Unexpected token " + ctx);
    }
}
