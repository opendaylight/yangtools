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
import com.google.common.annotations.VisibleForTesting;
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
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.ComplEscContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.PieceContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.PosCharGroupContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.QuantifierContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.RegExpContext;

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
                        checkState(((TerminalNode) child).getSymbol().getType() == regexLexer.Char,
                                "Unknown terminal child %s", child.toStringTree());
                        sb.append(child.getText());
                    } else {
                        checkState(child instanceof CharClassContext, "Unsupported child %s", child);
                        addCharClass(sb, (CharClassContext) child);
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
                sb.append(quantifier.getText());
            }
        }
    }

    private static void addCharClass(final StringBuilder sb, final CharClassContext charClass) {
        checkState(charClass.getChildCount() == 1, "Unhandled charClass shape %s", charClass);
        final ParseTree child = charClass.getChild(0);
        if (child instanceof CharClassEscContext) {
            addCharClassEsc(sb, (CharClassEscContext) child);
            return;
        }
        if (child instanceof CharClassExprContext) {
            addCharClassExpr(sb, (CharClassExprContext) child);
            return;
        }

        checkState(child instanceof TerminalNode, "Unsupported charClass %s", child);
        switch (((TerminalNode) child).getSymbol().getType()) {
            case regexLexer.WildcardEsc:
                sb.append(child.getText());
                break;
            default:
                throw new IllegalStateException("Unsupported terminal " + child.toStringTree());
        }
    }

    private static void addCharClassEsc(final StringBuilder sb, final CharClassEscContext charClassEsc) {
        checkState(charClassEsc.getChildCount() == 1, "Unhandled charClassExpr shape %s", charClassEsc);
        final ParseTree child = charClassEsc.getChild(0);

        if (child instanceof CatEscContext) {
            addEsc(sb, (CatEscContext) child, false);
            return;
        }
        if (child instanceof ComplEscContext) {
            addEsc(sb, (ComplEscContext) child, true);
            return;
        }

        checkState(child instanceof TerminalNode, "Unsupported escape %s", child);
        switch (((TerminalNode) child).getSymbol().getType()) {
            case regexLexer.SingleCharEsc:
                sb.append(child.getText());
                break;
            case regexLexer.MultiCharEsc:
                addMultiCharEsc(sb, child.getText());
                break;
            default:
                throw new IllegalStateException("Unsupported terminal escape " + child.toStringTree());
        }
    }

    private static void addMultiCharEsc(final StringBuilder sb, final String text) {
        checkState(text.length() == 2, "Unsupported character sequence %s", text);
        switch (text.charAt(1)) {
            case 'c':
                // XSD: XML NameChar
                sb.append('[').append(NAME_CHAR).append(']');
                break;
            case 'C':
                // XSD: [^\c]
                sb.append("[^").append(NAME_CHAR).append(']');
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
                // XSD:  [ \t\n\r]
                // Java: [ \t\n\x0B\f\r]
                sb.append('[').append(SPACE_CHAR).append(']');
                break;
            case 'S':
                // XSD:  [^ \t\n\r]
                // Java: [^ \t\n\x0B\f\r]
                sb.append("[^").append(SPACE_CHAR).append(']');
                break;
            case 'i':
                // XSD: XML NameStartChar
                sb.append('[').append(NAME_START_CHAR).append(']');
                break;
            case 'I':
                // XSD: [^\i]
                sb.append("[^").append(NAME_START_CHAR).append(']');
                break;
            case 'w':
                // XSD:  [^[\p{P}\p{Z}\p{C}]]
                //       (all characters except the set of "punctuation", "separator" and "other" characters)
                // Java: [\p{Alpha}\p{gc=Mn}\p{gc=Me}\p{gc=Mc}\p{Digit}\p{gc=Pc}\p{IsJoin_Control}]
                sb.append("[\\P{P}&&\\P{Z}&&\\P{C}]");
                break;
            case 'W':
                // XSD:  [^\w]
                // Java: [^\W]
                sb.append("[\\p{P}\\p{Z}\\p{C}]");
                break;
            default:
                throw new IllegalStateException("Unsupported char sequence " + text);
        }
    }

    private static void addEsc(final StringBuilder sb, final ParserRuleContext esc, final boolean complement) {
        checkState(esc.getChildCount() == 3, "Unsupported escape %s", esc);
        final CharPropContext charProp = getChild(esc, 1, CharPropContext.class);
        checkState(charProp.getChildCount() == 1, "Unsupported property %s", charProp);
        final ParseTree prop = charProp.getChild(0);
        checkState(prop instanceof TerminalNode, "Unsupported property class %s", prop);

        switch (((TerminalNode) prop).getSymbol().getType()) {
            case regexLexer.IsCategory:
                // Categories are fully supported by Java Pattern
                sb.append(esc.getText());
                break;
            case regexLexer.IsBlock:
                final String text = prop.getText();
                checkState(text.length() > 2, "Unsupported block property %s", text);
                sb.append(complement ? "\\P" : "\\p").append("{In").append(text, 2, text.length()).append('}');
                break;
            default:
                throw new IllegalStateException("Unsupported property type " + prop.toStringTree());
        }
    }

    private static void addCharClassExpr(final StringBuilder sb, final CharClassExprContext charClassExpr) {
        checkState(charClassExpr.getChildCount() == 3, "Unhandled charClassExpr shape %s", charClassExpr);
        final CharGroupContext charGroup = getChild(charClassExpr, 1, CharGroupContext.class);
        final ParseTree group = charGroup.getChild(0);

        checkState(group instanceof PosCharGroupContext, "Unsupported group %s", group);
        sb.append(charClassExpr.getChild(0).getText());
        addPosCharGroup(sb, (PosCharGroupContext) group);

        switch (charGroup.getChildCount()) {
            case 1:
                // No-op, we are done
                break;
            case 2:
                // Add the dash
                sb.append('-');
                break;
            case 3:
                // FIXME: handle subtraction
                throw new UnsupportedOperationException();
            case 4:
                // FIXME: handle subtraction
                throw new UnsupportedOperationException();
            default:
                throw new IllegalStateException("Unsupported group shape " + charGroup.toStringTree());
        }

        sb.append(']');
    }

    private static void addPosCharGroup(final StringBuilder sb, final PosCharGroupContext group) {
        // "&&" within a character classes has a special meaning in Java's Pattern, hence we need to escape them.
        // We track a single boolean, which indicates whether the last appended character was '&' and ignore further
        // ampersands if that is the case
        boolean lastWasAmpersand = false;
        for (ParseTree child : group.children) {
            // FIXME: this needs to be decomposed down, as CatEsc and ComplEsc and "&&" need to be transformed
//            if (child instanceof CharRangeContext) {
//
//            } else {
//                checkState(child instanceof CharClassEscContext, "Unsupported group child %s", child);
//
//            }

            sb.append(child.getText());
        }
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
        final ParseTree child = tree.getChild(offset);
        checkState(type.isInstance(child), "Unexpected child %s when expecting %s", child, type);
        return type.cast(child);
    }
}
