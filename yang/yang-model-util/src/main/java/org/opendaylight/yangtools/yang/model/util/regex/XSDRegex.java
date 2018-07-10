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
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.NegCharGroupContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.PieceContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.PosCharGroupContext;
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
                        sb.append(getTerminal(atom, 0, regexParser.NormalChar));
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
            case regexLexer.SingleCharEsc:
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
        checkState(((TerminalNode) child).getSymbol().getType() == regexLexer.MultiCharEsc,
                "Unsupported charClass type %s", child);
        final String text = child.getText();
        checkState(text.length() == 2, "Unsupported character sequence %s", text);
        switch (text.charAt(1)) {
            case 's':
                // XSD:  [ \t\n\r]
                // Java: [ \t\n\x0B\f\r]
                sb.append("[ \\t\\n\\r]");
                break;
            case 'S':
                // XSD:  [^ \t\n\r]
                // Java: [^ \t\n\x0B\f\r]
                sb.append("[^ \\t\\n\\r]");
                break;
            case 'i':
                // XSD: XML NameStartChar:
                //      ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] | [#x370-#x37D]
                //          | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF]
                //          | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
                sb.append("[:A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6xF8-\\u02FF\\u0370-\\u-37D\\u037F-\\u1FFF\\u200C-\\u200D"
                        + "\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD"
                        + "\\x{10000}-\\x{EFFFF}");
                break;
            case 'I':
                // XSD: [^\i]
                sb.append("[^:A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6xF8-\\u02FF\\u0370-\\u-37D\\u037F-\\u1FFF\\u200C-\\u200D"
                        + "\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD"
                        + "\\x{10000}-\\x{EFFFF}");
                break;
            case 'c':
                // XSD: XML NameChar:
                //      NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
                sb.append("[:A-Z_a-z0-9\\x2D\\x2E\\xB7\\xC0-\\xD6\\xD8-\\xF6xF8-\\u-37D\\u037F-\\u1FFF"
                        + "\\u200C-\\u200D\\u203F-\\u2040\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF"
                        + "\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\x{10000}-\\x{EFFFF}");
                break;
            case 'C':
                // XSD: [^\c]
                sb.append("[^:A-Z_a-z0-9\\x2D\\x2E\\xB7\\xC0-\\xD6\\xD8-\\xF6xF8-\\u-37D\\u037F-\\u1FFF"
                        + "\\u200C-\\u200D\\u203F-\\u2040\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF"
                        + "\\uF900-\\uFDCF\\uFDF0-\\uFFFD\\x{10000}-\\x{EFFFF}");
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

        sb.append('[');
        if (group instanceof PosCharGroupContext) {
            // FIXME: this needs to be decomposed down, as CatEsc and ComplEsc and "&&" need to be transformed
            sb.append(group.getText());
        } else {
            checkState(group instanceof NegCharGroupContext, "Unsupported group %s", group);
            // FIXME: this needs to be decomposed down, as CatEsc and ComplEsc and "&&" need to be transformed
            sb.append(group.getText());
        }
        sb.append(']');
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

    private static String getTerminal(final ParseTree tree, final int offset, final int type) {
        final ParseTree child = tree.getChild(offset);
        checkState(child instanceof TerminalNode, "Unexpected non-terminal %s", child);
        final TerminalNode terminal = (TerminalNode) child;
        checkState(terminal.getSymbol().getType() == type, "Unexpected terminal %s", terminal);
        return terminal.getText();
    }
}
