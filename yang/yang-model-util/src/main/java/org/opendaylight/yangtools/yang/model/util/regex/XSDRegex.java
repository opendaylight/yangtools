/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.regex;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.VerifyException;
import java.lang.Character.UnicodeBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.AtomContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.BranchContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.CatEscContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.CharClassContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.CharClassEscContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.CharClassExprContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.CharGroupContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.CharPropContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.CharRangeContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.ComplEscContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.PieceContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.PosCharGroupContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.QuantifierContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.RegExpContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.SeRangeContext;

/**
 * XSD regular expression.
 */
@Beta
public final class XSDRegex {
    // XML NameStartChar:
    //      ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D] | [#x37F-#x1FFF]
    //          | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF]
    //          | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
    @VisibleForTesting
    static final String NAME_START_CHAR = ":A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6xF8-\\u02FF\\u0370-\\u037D"
            + "\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF"
            + "\\uFDF0-\\uFFFD\\x{10000}-\\x{EFFFF}";
    // XSD: XML NameChar:
    //      NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
    @VisibleForTesting
    static final String NAME_CHAR = ":A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6xF8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF"
            + "\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD"
            + "\\x{10000}-\\x{EFFFF}";
    private static final String SPACE_CHAR = " \\t\\n\\r";

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

        final RegExpContext exp = parser.root().regExp();
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }

        return new XSDRegex(exp);
    }

    public Pattern toJavaPattern() {
        final StringBuilder sb = new StringBuilder().append('^');
        final int size = exp.getChildCount();
        if (size != 1) {
            // Multiple branches: enclose them in a non-capturing group
            sb.append("(?:");
            addRegExp(sb, exp, size);
        } else {
            // Single branch: just append that
            addBranch(sb, exp, 0);
        }

        final String regex = sb.append('$').toString();
        return Pattern.compile(regex);
    }

    @Override
    public String toString() {
        return exp.getText();
    }

    private static void addBranch(final StringBuilder sb, final RegExpContext regExp, final int offset) {
        final BranchContext branch = getChild(regExp, offset, BranchContext.class);

        for (PieceContext piece : branch.piece()) {
            final AtomContext atom = piece.atom();
            switch (atom.getChildCount()) {
                case 1:
                    final ParseTree child = atom.getChild(0);
                    if (child instanceof TerminalNode) {
                        final String text = verifyTerminal(child, regexLexer.Char).getText();
                        if ("^".equals(text) || "$".equals(text)) {
                            sb.append('\\');
                        }
                        sb.append(text);
                    } else {
                        addCharClass(sb, verifyTree(child, CharClassContext.class));
                    }
                    break;
                case 3:
                    addRegExp(sb, getChild(atom, 1, RegExpContext.class));
                    break;
                default:
                    throw verifyException(atom);
            }

            final QuantifierContext quantifier = piece.quantifier();
            if (quantifier != null) {
                sb.append(quantifier.getText());
            }
        }
    }

    private static void addCharClass(final StringBuilder sb, final CharClassContext charClass) {
        verifyChildren(charClass, 1);
        final ParseTree child = charClass.getChild(0);
        if (child instanceof CharClassEscContext) {
            addCharClassEsc(sb, (CharClassEscContext) child);
        } else if (child instanceof CharClassExprContext) {
            addCharClassExpr(sb, (CharClassExprContext) child, false);
        } else {
            verifyTerminal(child, regexLexer.WildcardEsc);
            sb.append(child.getText());
        }
    }

    private static void addCharClassEsc(final StringBuilder sb, final CharClassEscContext charClassEsc) {
        verifyChildren(charClassEsc, 1);
        final ParseTree child = charClassEsc.getChild(0);

        if (child instanceof CatEscContext) {
            addEsc(sb, (CatEscContext) child, false);
            return;
        }
        if (child instanceof ComplEscContext) {
            addEsc(sb, (ComplEscContext) child, true);
            return;
        }

        switch (verifyTerminal(child).getSymbol().getType()) {
            case regexLexer.SingleCharEsc:
            case regexLexer.NestedSingleCharEsc:
                sb.append(child.getText());
                break;
            case regexLexer.MultiCharEsc:
            case regexLexer.NestedMultiCharEsc:
                addMultiCharEsc(sb, child.getText());
                break;
            default:
                throw verifyException(child);
        }
    }

    private static void addMultiCharEsc(final StringBuilder sb, final String text) {
        verify(text.length() == 2, "Unsupported character sequence %s", text);
        switch (text.charAt(1)) {
            case 'c':
                // XSD: XML NameChar
                addNameChar(sb, false);
                break;
            case 'C':
                addNameChar(sb, true);
                break;
            case 'd':
                // XSD:  \p{Nd}
                // Java: [0-9]
                sb.append("\\p{Nd}");
                break;
            case 'D':
                // XSD:  \P{Nd}
                // Java: [^0-9]
                sb.append("\\P{Nd}");
                break;
            case 's':
                addSpace(sb, false);
                break;
            case 'S':
                addSpace(sb, true);
                break;
            case 'i':
                addNameStartChar(sb, false);
                break;
            case 'I':
                addNameStartChar(sb, true);
                break;
            case 'w':
                addWord(sb, false);
                break;
            case 'W':
                addWord(sb, true);
                break;
            default:
                throw new VerifyException("Unsupported char sequence " + text);
        }
    }

    private static StringBuilder addLeftBracket(final StringBuilder sb, final boolean negated) {
        if (negated) {
            return sb.append("[^");
        }
        return sb.append('[');
    }

    private static void addNameChar(final StringBuilder sb, final boolean negated) {
        // XSD: XML NameChar
        addLeftBracket(sb, negated).append(NAME_CHAR).append(']');
    }

    private static void addNameStartChar(final StringBuilder sb, final boolean negated) {
        // XSD: XML NameStartChar
        addLeftBracket(sb, negated).append(NAME_START_CHAR).append(']');
    }

    private static void addSpace(final StringBuilder sb, final boolean negated) {
        // XSD:  [ \t\n\r]
        // Java: [ \t\n\x0B\f\r]
        addLeftBracket(sb, negated).append(SPACE_CHAR).append(']');
    }

    private static void addWord(final StringBuilder sb, final boolean negated) {
        // XSD:  [^[\p{P}\p{Z}\p{C}]]
        //       (all characters except the set of "punctuation", "separator" and "other" characters)
        // Java: [\p{Alpha}\p{gc=Mn}\p{gc=Me}\p{gc=Mc}\p{Digit}\p{gc=Pc}\p{IsJoin_Control}]
        // These are hand-crafted strings inlining negation
        sb.append(negated ? "[\\p{P}\\p{Z}\\p{C}]" : "[\\P{P}&&\\P{Z}&&\\P{C}]");
    }

    private static void addEsc(final StringBuilder sb, final ParserRuleContext esc, final boolean complement) {
        verifyChildren(esc, 3);
        final CharPropContext charProp = getChild(esc, 1, CharPropContext.class);
        verifyChildren(charProp, 1);
        final TerminalNode prop = verifyTerminal(charProp.getChild(0));
        switch (prop.getSymbol().getType()) {
            case regexLexer.IsCategory:
                // Categories are fully supported by Java Pattern
                sb.append(esc.getText());
                break;
            case regexLexer.IsBlock:
                final String text = prop.getText();
                verify(text.length() > 2, "Unsupported block property %s", text);

                final String name = text.substring(2);
                try {
                    UnicodeBlock.forName(name);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unicode block named \"" + name + " is not supported", e);
                }

                sb.append(complement ? "\\P" : "\\p").append("{In").append(text, 2, text.length()).append('}');
                break;
            default:
                throw verifyException(prop);
        }
    }

    private static void addCharClassExpr(final StringBuilder sb, final CharClassExprContext charClassExpr,
            final boolean negate) {
        verifyChildren(charClassExpr, 3);

        final TerminalNode first = verifyTerminal(charClassExpr.getChild(0));
        final boolean negGroup;
        switch (first.getSymbol().getType()) {
            case regexLexer.NegCharGroup:
            case regexLexer.NestedNegCharGroup:
                negGroup = !negate;
                break;
            case regexLexer.PosCharGroup:
            case regexLexer.NestedPosCharGroup:
                negGroup = negate;
                break;
            default:
                throw verifyException(first);
        }
        addLeftBracket(sb, negGroup);

        final CharGroupContext charGroup = getChild(charClassExpr, 1, CharGroupContext.class);
        final int size = charGroup.getChildCount();
        final ParseTree group = charGroup.getChild(0);

        if (group instanceof PosCharGroupContext) {
            addPosCharGroup(sb, (PosCharGroupContext) group);
            switch (size) {
                case 1:
                    // No-op
                    break;
                case 2:
                    sb.append('-');
                    break;
                case 3:
                    addNegatedChild(sb, charGroup, 2);
                    break;
                case 4:
                    addNegatedChild(sb.append("\\-"), charGroup, 3);
                    break;
                default:
                    throw verifyException(charGroup);
            }
        } else {
            verifyTerminal(group, regexLexer.DASH);
            switch (size) {
                case 1:
                    sb.append('-');
                    break;
                case 3:
                    addNegatedChild(sb.append("\\-"), charGroup, 2);
                    break;
                default:
                    throw verifyException(charGroup);
            }
        }

        sb.append(']');
    }

    private static void addNegatedChild(final StringBuilder sb, final CharGroupContext charGroup, final int offset) {
        addCharClassExpr(sb.append("&&"), getChild(charGroup, offset, CharClassExprContext.class), true);
    }

    private static void addPosCharGroup(final StringBuilder sb, final PosCharGroupContext group) {
        // "&&" within a character classes has a special meaning in Java's Pattern, hence we need to escape them.
        // We track a single boolean, which indicates whether we have seen an ampersand, if that is the case, we
        // suppress further emission of it.
        boolean seenAmpersand = false;
        for (ParseTree child : group.children) {
            if (child instanceof CharClassEscContext) {
                addCharClassEsc(sb, verifyTree(child, CharClassEscContext.class));
            } else if (child instanceof CharRangeContext) {
                seenAmpersand |= addCharRange(sb, verifyTree(child, CharRangeContext.class), seenAmpersand);
            } else {
                verifyTerminal(child, regexLexer.DASH);
                sb.append('-');
            }
        }
    }

    private static boolean addCharRange(final StringBuilder sb, final CharRangeContext charRange,
            final boolean seenAmpersand) {
        verifyChildren(charRange, 1);
        final ParseTree child = charRange.getChild(0);
        final String text = child.getText();
        if (child instanceof SeRangeContext) {
            sb.append(text);
            return false;
        }

        verifyTerminal(child, regexParser.XmlChar);
        final boolean isAmpersand = "&".equals(text);
        if (!seenAmpersand || !isAmpersand) {
            sb.append(text);
        }
        return isAmpersand;
    }

    private static void addRegExp(final StringBuilder sb, final RegExpContext regExp) {
        sb.append('(');
        final int size = regExp.getChildCount();
        addRegExp(sb, regExp, size);
    }

    private static void addRegExp(final StringBuilder sb, final RegExpContext regExp, final int size) {
        addBranch(sb, regExp, 0);
        for (int i = 2; i < size; i += 2) {
            sb.append('|');
            addBranch(sb, regExp, i);
        }
        sb.append(')');
    }

    private static <T> T getChild(final ParseTree tree, final int offset, final Class<T> type) {
        return verifyTree(tree.getChild(offset), type);
    }

    private static void verifyChildren(final ParseTree tree, final int expected) {
        if (tree.getChildCount() != expected) {
            throw new VerifyException("Expecting " + expected + " child(ren) in " + tree.toStringTree());
        }
    }

    private static VerifyException verifyException(final ParseTree tree) {
        return new VerifyException("Unexpected tree " + tree.toStringTree());
    }

    private static TerminalNode verifyTerminal(final ParseTree tree) {
        return verifyTree(tree, TerminalNode.class);
    }

    private static TerminalNode verifyTerminal(final ParseTree tree, final int expected) {
        final TerminalNode terminal = verifyTerminal(tree);
        final int type = terminal.getSymbol().getType();
        if (type != expected) {
            throw new VerifyException("Terminal type " + regexLexer.VOCABULARY.getSymbolicName(expected) + "expected,"
                + regexLexer.VOCABULARY.getSymbolicName(type) + " found");
        }
        return terminal;
    }

    private static <T> T verifyTree(final ParseTree tree, final Class<T> expected) {
        if (expected.isInstance(tree)) {
            return expected.cast(tree);
        }
        throw verifyException(tree);
    }
}
