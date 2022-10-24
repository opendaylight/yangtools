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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
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
import org.opendaylight.yangtools.yang.xpath.antlr.instanceIdentifierParser.QuotedStringContext;
import org.opendaylight.yangtools.yang.xpath.antlr.xpathLexer;
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
        YangQNameExpr createExpr(final @Nullable String prefix, final String localName) {
            return YangQNameExpr.of(createQName(prefix, localName));
        }

        @Override
        QNameStep createChildStep(final @Nullable String prefix, final String localName,
                final Collection<YangExpr> predicates) {
            return YangXPathAxis.CHILD.asStep(createQName(prefix, localName), predicates);
        }

        private static UnresolvedQName createQName(final @Nullable String prefix, final String localName) {
            return prefix == null ? UnresolvedQName.Unqualified.of(localName)
                : UnresolvedQName.Qualified.of(prefix, localName);
        }
    }

    static final class Qualified extends InstanceIdentifierParser {
        private final YangNamespaceContext namespaceContext;

        Qualified(final YangXPathMathMode mathMode, final YangNamespaceContext namespaceContext) {
            super(mathMode);
            this.namespaceContext = requireNonNull(namespaceContext);
        }

        @Override
        YangQNameExpr createExpr(final @Nullable String prefix, final String localName) {
            return prefix == null ? YangQNameExpr.of(UnresolvedQName.Unqualified.of(localName))
                : YangQNameExpr.of(namespaceContext.createQName(prefix, localName));
        }

        @Override
        QNameStep createChildStep(final @Nullable String prefix, final String localName,
                final Collection<YangExpr> predicates) {
            return prefix == null ? YangXPathAxis.CHILD.asStep(UnresolvedQName.Unqualified.of(localName))
                : YangXPathAxis.CHILD.asStep(namespaceContext.createQName(prefix, localName), predicates);
        }
    }

    static final class Unqualified extends InstanceIdentifierParser {
        private final YangNamespaceContext namespaceContext;
        private final QNameModule defaultNamespace;

        Unqualified(final YangXPathMathMode mathMode, final YangNamespaceContext namespaceContext,
                final QNameModule defaultNamespace) {
            super(mathMode);
            this.namespaceContext = requireNonNull(namespaceContext);
            this.defaultNamespace = requireNonNull(defaultNamespace);
        }

        @Override
        YangQNameExpr createExpr(final @Nullable String prefix, final String localName) {
            return YangQNameExpr.of(createQName(prefix, localName));
        }

        @Override
        QNameStep createChildStep(final @Nullable String prefix, final String localName,
                final Collection<YangExpr> predicates) {
            return YangXPathAxis.CHILD.asStep(createQName(prefix, localName), predicates);
        }

        private QName createQName(final @Nullable String prefix, final String localName) {
            return prefix == null ? QName.create(defaultNamespace, localName)
                : namespaceContext.createQName(prefix, localName);
        }
    }

    private final YangXPathMathSupport mathSupport;

    InstanceIdentifierParser(final YangXPathMathMode mathMode) {
        mathSupport = mathMode.getSupport();
    }

    final Absolute interpretAsInstanceIdentifier(final YangLiteralExpr expr) throws XPathExpressionException {
        final xpathLexer lexer = new xpathLexer(CharStreams.fromString(expr.getLiteral()));
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

    abstract YangQNameExpr createExpr(@Nullable String prefix, String localName);

    abstract QNameStep createChildStep(@Nullable String prefix, String localName, Collection<YangExpr> predicates);

    private QNameStep parsePathArgument(final PathArgumentContext expr) {
        final NodeIdentifierContext childExpr = getChild(expr, NodeIdentifierContext.class, 0);
        final String prefix;
        final String localName;
        switch (childExpr.getChildCount()) {
            case 1:
                prefix = null;
                localName = verifyIdentifier(childExpr, 0);
                break;
            case 3:
                prefix = verifyIdentifier(childExpr, 0);
                localName = verifyIdentifier(childExpr, 2);
                break;
            default:
                throw illegalShape(expr);
        }

        return switch (expr.getChildCount()) {
            case 1 -> createChildStep(prefix, localName, ImmutableSet.of());
            case 2 -> createChildStep(prefix, localName, parsePredicate(getChild(expr, PredicateContext.class, 1)));
            default -> throw illegalShape(expr);
        };
    }

    private Collection<YangExpr> parsePredicate(final PredicateContext expr) {
        final ParseTree first = expr.getChild(0);
        if (first instanceof LeafListPredicateContext leafListPredicate) {
            return ImmutableSet.of(YangBinaryOperator.EQUALS.exprWith(YangLocationPath.self(),
                parseEqStringValue(getChild(leafListPredicate.getChild(LeafListPredicateExprContext.class, 0),
                    EqQuotedStringContext.class, 1))));
        } else if (first instanceof PosContext posContext) {
            return ImmutableSet.of(YangBinaryOperator.EQUALS.exprWith(FunctionSupport.POSITION,
                mathSupport.createNumber(posContext.getToken(instanceIdentifierParser.PositiveIntegerValue, 0)
                    .getText())));
        }

        final int length = expr.getChildCount();
        final var ret = new ArrayList<YangExpr>(length);
        for (int i = 0; i < length; ++i) {
            final var pred = getChild(expr, KeyPredicateContext.class, i).getChild(KeyPredicateExprContext.class, 0);
            ret.add(YangBinaryOperator.EQUALS.exprWith(
                createChildExpr(getChild(pred, NodeIdentifierContext.class, 0)),
                parseEqStringValue(getChild(pred, EqQuotedStringContext.class, 1))));

        }

        return ret;
    }

    private YangQNameExpr createChildExpr(final NodeIdentifierContext expr) {
        return createExpr(verifyIdentifier(expr, 0), verifyIdentifier(expr, 2));
    }

    private static String verifyIdentifier(final NodeIdentifierContext expr, final int child) {
        return verifyToken(expr, child, instanceIdentifierParser.Identifier).getText();
    }

    private static YangLiteralExpr parseEqStringValue(final EqQuotedStringContext expr) {
        return YangLiteralExpr.of(verifyToken(getChild(expr, QuotedStringContext.class, expr.getChildCount() - 1), 1,
            instanceIdentifierParser.STRING).getText());
    }
}
