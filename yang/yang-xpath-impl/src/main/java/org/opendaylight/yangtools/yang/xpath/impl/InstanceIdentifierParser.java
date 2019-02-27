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
import static org.opendaylight.yangtools.yang.xpath.impl.ParseTreeUtils.verifyToken;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathSupport;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.EqQuotedStringContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.InstanceIdentifierContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.KeyPredicateContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.KeyPredicateExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.LeafListPredicateContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.LeafListPredicateExprContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.NodeIdentifierContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.PathArgumentContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.PosContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.PredicateContext;
import org.opendaylight.yangtools.yang.xpath.impl.instanceIdentifierParser.QuotedStringContext;

final class InstanceIdentifierParser {
    private final YangNamespaceContext namespaceContext;
    private final YangXPathMathSupport mathSupport;

    InstanceIdentifierParser(final YangNamespaceContext namespaceContext, final YangXPathMathMode mathMode) {
        this.namespaceContext = requireNonNull(namespaceContext);
        this.mathSupport = mathMode.getSupport();
    }

    YangLocationPath interpretAsInstanceIdentifier(final YangLiteralExpr expr) throws XPathExpressionException {
        final xpathLexer lexer = new xpathLexer(CharStreams.fromString(expr.getLiteral()));
        final instanceIdentifierParser parser = new instanceIdentifierParser(new CommonTokenStream(lexer));
        lexer.removeErrorListeners();
        parser.removeErrorListeners();

        // FIXME: add listeners

        final InstanceIdentifierContext id = parser.instanceIdentifier();
        final int length = id.getChildCount();
        final List<Step> steps = new ArrayList<>(length / 2);
        for (int i = 1; i < length; i += 2) {
            steps.add(parsePathArgument(getChild(id, PathArgumentContext.class, i)));
        }

        return YangLocationPath.of(true, steps);
    }

    private Step parsePathArgument(final PathArgumentContext expr) {
        final QName qname = parseQName(getChild(expr, NodeIdentifierContext.class, 0));
        switch (expr.getChildCount()) {
            case 1:
                return YangXPathAxis.CHILD.asStep(qname, ImmutableSet.of());
            case 2:
                return YangXPathAxis.CHILD.asStep(qname, parsePredicate(getChild(expr, PredicateContext.class, 1)));
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
            return ImmutableSet.of(YangBinaryOperator.EQUALS.exprWith(FunctionSupport.POSITION, mathSupport.createNumber(
                ((PosContext) first).getToken(instanceIdentifierParser.PositiveIntegerValue, 0).getText())));
        }

        final int length = expr.getChildCount();
        final List<YangExpr> ret = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            final KeyPredicateExprContext pred = getChild(expr, KeyPredicateContext.class, i)
                    .getChild(KeyPredicateExprContext.class, 0);
            ret.add(YangBinaryOperator.EQUALS.exprWith(
                YangQNameExpr.of(parseQName(getChild(pred, NodeIdentifierContext.class, 0))),
                parseEqStringValue(getChild(pred, EqQuotedStringContext.class, 1))));

        }

        return ret;
    }

    private QName parseQName(final NodeIdentifierContext expr) {
        return namespaceContext.createQName(
            verifyToken(expr, 0, instanceIdentifierParser.Identifier).getText(),
            verifyToken(expr, 2, instanceIdentifierParser.Identifier).getText());
    }

    private static YangLiteralExpr parseEqStringValue(final EqQuotedStringContext expr) {
        return YangLiteralExpr.of(verifyToken(getChild(expr, QuotedStringContext.class, expr.getChildCount() - 1), 1,
            instanceIdentifierParser.STRING).getText());
    }
}
