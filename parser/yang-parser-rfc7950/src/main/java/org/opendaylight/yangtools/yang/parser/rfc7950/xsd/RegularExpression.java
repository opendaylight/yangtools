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
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.BranchContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassEscContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.CharClassExprContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.PieceContext;
import org.opendaylight.yangtools.yang.parser.antlr.regexParser.PosCharGroupContext;
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
        final var first = ctx.children.getFirst();
        return switch (first) {
            case CharClassEscContext esc -> parseQuantifier(ctx, parseCharClassEscape(esc), 1);
            case CharClassExprContext expr -> parseQuantifier(ctx, parseCharClassExpr(expr), 1);
            case TerminalNode terminal -> switch (terminal.getSymbol().getType()) {
                case Char -> parseQuantifier(ctx, new NormalCharacter(terminal.getText()), 1);
                case WildcardEsc -> parseQuantifier(ctx, Dot.INSTANCE, 1);
                case LPAREN -> {
                    childToken(ctx, 2, RPAREN);
                    yield parseQuantifier(ctx,
                        new ParenRegularExpression(parseRegExp(child(ctx, 1, RegExpContext.class))), 3);
                }
                default -> throw unexpectedToken(terminal);
            };
            default -> throw unexpectedChild(first);
        };
    }

    private static Piece parseQuantifier(final PieceContext ctx, final Atom atom, final int offset) {
        final var size = ctx.getChildCount();
        if (size == offset) {
            return new Piece(atom, null);
        }
        final var first = verifyChildTerminal(ctx.children.get(offset));
        return new Piece(atom, switch (first.getType()) {
                case PLUS -> Plus.INSTANCE;
                case QUESTION -> Question.INSTANCE;
                case STAR -> Star.INSTANCE;
                case StartQuantity -> parseQuantity(ctx, offset + 1, size);
                default -> throw unexpectedToken(first);
            });
    }

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
        final var first = verifyChildTerminal(ctx.children.getFirst());
        final int size = ctx.getChildCount();
        childToken(ctx, size - 1, EndCharGroup);
        final var charGroup = parseCharGroup(ctx, size);
        return switch (first.getType()) {
            case NegCharGroup, NestedNegCharGroup -> new CharacterClassExpression.Negative(charGroup);
            case PosCharGroup, NestedPosCharGroup -> new CharacterClassExpression.Positive(charGroup);
            default -> throw unexpectedToken(first);
        };
    }

    private static CharacterGroup parseCharGroup(final CharClassExprContext ctx, final int size) {
        final var first = ctx.children.get(1);
        return switch (first) {
            case PosCharGroupContext posCharGroup -> {
                final var firstGroup = parsePosCharGroup(posCharGroup);
                yield switch (size) {
                        case 3 -> firstGroup;
                        case 4 -> {
                            childToken(ctx, 2, DASH);
                            yield union(firstGroup, SimpleCharacterGroup.DASH);
                        }
                        case 5 -> {
                            childToken(ctx, 2, DASH);
                            yield new DifferenceCharacterGroup(firstGroup,
                                parseCharClassExpr(child(ctx, 3, CharClassExprContext.class)));
                        }
                        case 6 -> {
                            childToken(ctx, 2, DASH);
                            childToken(ctx, 3, DASH);
                            yield new DifferenceCharacterGroup(union(firstGroup, SimpleCharacterGroup.DASH),
                                parseCharClassExpr(child(ctx, 4, CharClassExprContext.class)));
                        }
                        default -> throw unexpectedShape(ctx);
                    };
            }
            case TerminalNode terminal -> {
                verifyToken(terminal, DASH);
                yield switch(size) {
                        case 3 -> SimpleCharacterGroup.DASH;
                        case 5 -> {
                            childToken(ctx, 2, DASH);
                            yield new DifferenceCharacterGroup(SimpleCharacterGroup.DASH,
                                parseCharClassExpr(child(ctx, 3, CharClassExprContext.class)));
                        }
                        default -> throw unexpectedShape(ctx);
                    };
            }
            default -> throw unexpectedChild(first);
        };
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
        if (children.getFirst() instanceof TerminalNode terminal) {
            if (terminal.getSymbol().getType() != DASH) {
                throw unexpectedToken(terminal);
            }
            components.add(SimpleCharacterGroup.DASH);
            offset = 1;
        } else {
            offset = 0;
        }

        do {
            final var first = children.get(offset++);
            switch (first) {
                case CharClassEscContext charClassEsc -> {
                    components.add(parseCharClassEscape(charClassEsc));
                }
                case TerminalNode terminal -> {
                    offset += appendPosCharGroupItem(components, terminal.getSymbol(), children, offset, size);
                }
                default -> throw unexpectedChild(first);
            }
        } while (offset < size);

        // TODO: merge spans of SimpleCharacterGroups
        return components.size() == 1 ? components.getFirst() : new UnionCharacterGroup(components);
    }

    private static int appendPosCharGroupItem(final ArrayList<CharacterGroup> components, final Token first,
            final List<ParseTree> children, final int offset, final int size) {
        if (offset == size) {
            components.add(new SimpleCharacterGroup(verifyToken(first, XmlChar).getText()));
            return 0;
        }
        verifyToken(childToken(children, offset + 1), DASH);
        components.add(new RangeCharacterGroup(parseCharOrEsc(first),
            parseCharOrEsc(childToken(children, offset + 2))));
        return 2;
    }

    private static String parseCharOrEsc(final Token token) {
        return switch (token.getType()) {
            case SingleCharEsc, XmlChar -> token.getText();
            default -> throw unexpectedToken(token);
        };
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
