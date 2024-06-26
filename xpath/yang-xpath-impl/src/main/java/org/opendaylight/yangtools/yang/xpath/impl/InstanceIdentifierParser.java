/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.getChild;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.illegalShape;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.verifyTerminal;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.verifyToken;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.xpath.XPathExpressionException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierLexer;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.EqQuotedStringContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.KeyPredicateContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.KeyPredicateExprContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.LeafListPredicateContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.LeafListPredicateExprContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.NodeIdentifierContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.PathArgumentContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.PosContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.PredicateContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.QuotedStringContext;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Absolute;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathSupport;

abstract class InstanceIdentifierParser {
    static final class Base extends InstanceIdentifierParser {
        Base(final YangXPathMathMode mathMode) {
            super(mathMode);
        }

        @Override
        YangQNameExpr createExpr(final String localName) {
            return YangQNameExpr.of(UnresolvedQName.Unqualified.of(localName));
        }

        @Override
        YangQNameExpr createExpr(final String prefix, final String localName) {
            return YangQNameExpr.of(UnresolvedQName.Qualified.of(prefix, localName));
        }

        @Override
        QNameStep createChildStep(final String localName, final Collection<YangExpr> predicates) {
            return YangXPathAxis.CHILD.asStep(UnresolvedQName.Unqualified.of(localName), predicates);
        }

        @Override
        QNameStep createChildStep(final String prefix, final String localName, final Collection<YangExpr> predicates) {
            return YangXPathAxis.CHILD.asStep(UnresolvedQName.Qualified.of(prefix, localName), predicates);
        }
    }

    static final class Qualified extends InstanceIdentifierParser {
        private final YangNamespaceContext namespaceContext;

        Qualified(final YangXPathMathMode mathMode, final YangNamespaceContext namespaceContext) {
            super(mathMode);
            this.namespaceContext = requireNonNull(namespaceContext);
        }

        @Override
        YangQNameExpr createExpr(final String localName) {
            return YangQNameExpr.of(UnresolvedQName.Unqualified.of(localName));
        }

        @Override
        YangQNameExpr createExpr(final String prefix, final String localName) {
            return YangQNameExpr.of(namespaceContext.createQName(prefix, localName));
        }

        @Override
        QNameStep createChildStep(final String localName, final Collection<YangExpr> predicates) {
            return YangXPathAxis.CHILD.asStep(UnresolvedQName.Unqualified.of(localName), predicates);
        }

        @Override
        QNameStep createChildStep(final String prefix, final String localName, final Collection<YangExpr> predicates) {
            return YangXPathAxis.CHILD.asStep(namespaceContext.createQName(prefix, localName), predicates);
        }
    }

    private final YangXPathMathSupport mathSupport;

    InstanceIdentifierParser(final YangXPathMathMode mathMode) {
        mathSupport = mathMode.getSupport();
    }

    final Absolute interpretAsInstanceIdentifier(final YangLiteralExpr expr) throws XPathExpressionException {
        final var lexer = new instanceIdentifierLexer(CharStreams.fromString(expr.getLiteral()));
        final var parser = new instanceIdentifierParser(new CommonTokenStream(lexer));
        final var listener = new CapturingErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        final var id = parser.instanceIdentifier();
        listener.reportError();

        final int length = id.getChildCount();
        final var steps = new ArrayList<Step>(length / 2);
        for (int i = 1; i < length; i += 2) {
            steps.add(parsePathArgument(getChild(id, PathArgumentContext.class, i)));
        }

        return YangLocationPath.absolute(steps);
    }

    abstract YangQNameExpr createExpr(String localName);

    abstract YangQNameExpr createExpr(String prefix, String localName);

    abstract QNameStep createChildStep(String localName, Collection<YangExpr> predicates);

    abstract QNameStep createChildStep(String prefix, String localName, Collection<YangExpr> predicates);

    private QNameStep parsePathArgument(final PathArgumentContext expr) {
        final var predicates = switch (expr.getChildCount()) {
            case 1 -> ImmutableSet.<YangExpr>of();
            case 2 -> parsePredicate(getChild(expr, PredicateContext.class, 1));
            default -> throw illegalShape(expr);
        };

        final NodeIdentifierContext childExpr = getChild(expr, NodeIdentifierContext.class, 0);
        final String first = verifyIdentifier(childExpr, 0);

        return switch (childExpr.getChildCount()) {
            case 1 -> createChildStep(first, predicates);
            case 3 -> createChildStep(first, verifyIdentifier(childExpr, 2), predicates);
            default -> throw illegalShape(childExpr);
        };
    }

    private Collection<YangExpr> parsePredicate(final PredicateContext expr) {
        return switch (expr.getChild(0)) {
            case LeafListPredicateContext llp ->
                ImmutableSet.of(YangBinaryOperator.EQUALS.exprWith(YangLocationPath.self(), parseEqStringValue(
                    getChild(llp.getChild(LeafListPredicateExprContext.class, 0),
                        EqQuotedStringContext.class, 1))));
            case PosContext pc ->
                ImmutableSet.of(YangBinaryOperator.EQUALS.exprWith(FunctionSupport.POSITION, mathSupport.createNumber(
                    pc.getToken(instanceIdentifierParser.PositiveIntegerValue, 0).getText())));
            default -> {
                final int length = expr.getChildCount();
                final var ret = new ArrayList<YangExpr>(length);
                for (int i = 0; i < length; ++i) {
                    final var pred = getChild(expr, KeyPredicateContext.class, i)
                        .getChild(KeyPredicateExprContext.class, 0);
                    ret.add(YangBinaryOperator.EQUALS.exprWith(
                        createChildExpr(getChild(pred, NodeIdentifierContext.class, 0)),
                        parseEqStringValue(getChild(pred, EqQuotedStringContext.class, 1))));
                }
                yield ret;
            }
        };
    }

    private YangQNameExpr createChildExpr(final NodeIdentifierContext expr) {
        final var first = verifyIdentifier(expr, 0);
        return switch (expr.getChildCount()) {
            case 1 -> createExpr(first);
            case 3 -> createExpr(first, verifyIdentifier(expr, 2));
            default -> throw illegalShape(expr);
        };
    }

    private static String verifyIdentifier(final NodeIdentifierContext expr, final int child) {
        return verifyToken(expr, child, instanceIdentifierParser.Identifier).getText();
    }

    private static YangLiteralExpr parseEqStringValue(final EqQuotedStringContext expr) {
        final var quotedString = getChild(expr, QuotedStringContext.class, expr.getChildCount() - 1);
        return switch (quotedString.getChildCount()) {
            case 1 -> YangLiteralExpr.empty();
            case 2 -> {
                final var terminal = verifyTerminal(quotedString.getChild(0));
                final var token = terminal.getSymbol();
                final var literal = switch (token.getType()) {
                    case instanceIdentifierParser.DQUOT_STRING -> unescape(token.getText());
                    case instanceIdentifierParser.SQUOT_STRING -> token.getText();
                    default -> throw illegalShape(terminal);
                };
                yield YangLiteralExpr.of(literal);
            }
            default -> throw illegalShape(quotedString);
        };
    }

    private static String unescape(final String escaped) {
        final int firstEscape = escaped.indexOf('\\');
        return firstEscape == -1 ? escaped : unescape(escaped, firstEscape);
    }

    private static String unescape(final String escaped, final int firstEscape) {
        // Sizing: optimize allocation for only a single escape
        final int length = escaped.length();
        final var sb = new StringBuilder(length - 1).append(escaped, 0, firstEscape);

        int esc = firstEscape;
        while (true) {
            final var ch = escaped.charAt(esc + 1);
            sb.append(
                switch (ch) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> throw new VerifyException("Unexpected escaped char '" + ch + "'");
                });

            final int start = esc + 2;
            esc = escaped.indexOf('\\', start);
            if (esc == -1) {
                return sb.append(escaped, start, length).toString();
            }

            sb.append(escaped, start, esc);
        }
    }
}
