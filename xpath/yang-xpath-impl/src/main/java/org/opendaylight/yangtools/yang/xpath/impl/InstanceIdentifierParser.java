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
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.getToken;
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.illegalShape;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.xml.xpath.XPathExpressionException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierLexer;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.EqQuotedStringContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.InstanceIdentifierContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.KeyPredicateContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.KeyPredicateExprContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.LeafListPredicateContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.LeafListPredicateExprContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.NodeIdentifierContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.PathArgumentContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.PosContext;
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.PredicateContext;
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
        YangQNameExpr createExpr(final String prefix, @Nullable final String localName) {
            return YangQNameExpr.of(qnameOf(prefix, localName));
        }

        @Override
        QNameStep createChildStep(final String prefix, @Nullable final String localName,
                final Collection<YangExpr> predicates) {
            return YangXPathAxis.CHILD.asStep(qnameOf(prefix, localName), predicates);
        }

        private static UnresolvedQName qnameOf(final String first, final @Nullable String second) {
            return second == null ? UnresolvedQName.Unqualified.of(first) : UnresolvedQName.Qualified.of(first, second);
        }
    }

    static final class Qualified extends InstanceIdentifierParser {
        final YangNamespaceContext namespaceContext;

        Qualified(final YangXPathMathMode mathMode, final YangNamespaceContext namespaceContext) {
            super(mathMode);
            this.namespaceContext = requireNonNull(namespaceContext);
        }

        @Override
        YangQNameExpr createExpr(final String prefix, @Nullable final String localName) {
            return localName == null
                    ? YangQNameExpr.of(UnresolvedQName.Unqualified.of(prefix))
                    : YangQNameExpr.of(namespaceContext.createQName(prefix, localName));
        }

        @Override
        QNameStep createChildStep(final String prefix, @Nullable final String localName,
                final Collection<YangExpr> predicates) {
            return localName == null
                    ? YangXPathAxis.CHILD.asStep(UnresolvedQName.Unqualified.of(prefix), predicates)
                    : YangXPathAxis.CHILD.asStep(namespaceContext.createQName(prefix, localName), predicates);
        }
    }

    private final YangXPathMathSupport mathSupport;

    InstanceIdentifierParser(final YangXPathMathMode mathMode) {
        mathSupport = mathMode.getSupport();
    }

    final Absolute interpretAsInstanceIdentifier(final YangLiteralExpr expr) throws XPathExpressionException {
        final instanceIdentifierLexer lexer = new instanceIdentifierLexer(CharStreams.fromString(expr.getLiteral()));
        final instanceIdentifierParser parser = new instanceIdentifierParser(new CommonTokenStream(lexer));
        final CapturingErrorListener listener = new CapturingErrorListener();
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        final InstanceIdentifierContext id = parser.instanceIdentifier();
        listener.reportError();

        final int length = id.getChildCount();
        final List<Step> steps = new ArrayList<>(length / 2);
        for (int i = 1; i < length; i += 2) {
            steps.add(parsePathArgument(getChild(id, PathArgumentContext.class, i)));
        }
        return YangLocationPath.absolute(steps);
    }

    abstract YangQNameExpr createExpr(String prefix, String localName);

    abstract QNameStep createChildStep(String prefix, String localName, Collection<YangExpr> predicates);

    private QNameStep parsePathArgument(final PathArgumentContext expr) {
        final NodeIdentifierContext childExpr = getChild(expr, NodeIdentifierContext.class, 0);
        final String prefix = verifyIdentifier(childExpr, 0);
        final String localName = childExpr.getChildCount() == 1 ? null : verifyIdentifier(childExpr, 1);

        switch (expr.getChildCount()) {
            case 1:
                return createChildStep(prefix, localName, Set.of());
            case 2:
                return createChildStep(prefix, localName, parsePredicate(getChild(expr, PredicateContext.class, 1)));
            default:
                throw illegalShape(expr);
        }
    }

    private Collection<YangExpr> parsePredicate(final PredicateContext expr) {
        final ParseTree first = expr.getChild(0);
        if (first instanceof LeafListPredicateContext) {
            return ImmutableSet.of(YangBinaryOperator.EQUALS.exprWith(YangLocationPath.self(),
                    parseEqStringValue(getChild(((LeafListPredicateContext) first)
                            .getChild(LeafListPredicateExprContext.class, 0), EqQuotedStringContext.class, 1))));
        } else if (first instanceof PosContext) {
            return ImmutableSet.of(YangBinaryOperator.EQUALS.exprWith(FunctionSupport.POSITION,
                    mathSupport.createNumber(((PosContext) first).getToken(instanceIdentifierParser.POS_INDEX, 0)
                            .getText())));
        }

        final int length = expr.getChildCount();
        final List<YangExpr> ret = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            final KeyPredicateExprContext pred = getChild(expr, KeyPredicateContext.class, i)
                    .getChild(KeyPredicateExprContext.class, 0);
            ret.add(YangBinaryOperator.EQUALS.exprWith(
                    createChildExpr(getChild(pred, NodeIdentifierContext.class, 0)),
                    parseEqStringValue(getChild(pred, EqQuotedStringContext.class, 1))));
        }
        return ret;
    }

    private YangQNameExpr createChildExpr(final NodeIdentifierContext expr) {
        final String prefix = verifyIdentifier(expr, 0);
        final String localName = expr.getChildCount() == 1 ? null : verifyIdentifier(expr, 1);
        return createExpr(prefix, localName);
    }

    private static String verifyIdentifier(final NodeIdentifierContext expr, final int occurrence) {
        return getToken(expr, instanceIdentifierParser.IDENTIFIER, occurrence).getText();
    }

    private static YangLiteralExpr parseEqStringValue(final EqQuotedStringContext expr) {
        final String quotedString = getToken(expr, instanceIdentifierParser.QUOTED_STRING, 0).getText();
        return YangLiteralExpr.of(unQuoteString(quotedString));
    }

    private static String unQuoteString(String quotedString) {
        return quotedString.startsWith("\"")
                ? quotedString.replaceAll("^\"|\"$", "").replace("\\\"", "\"")
                : quotedString.replaceAll("^'|'$", "").replace("\\'", "'");
    }
}
