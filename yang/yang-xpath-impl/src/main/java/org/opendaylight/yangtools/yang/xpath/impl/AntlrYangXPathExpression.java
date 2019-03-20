/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static java.util.Objects.requireNonNull;

import javax.xml.xpath.XPathExpressionException;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr.Resolved;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;

abstract class AntlrYangXPathExpression implements YangXPathExpression {
    static final class Base extends AntlrYangXPathExpression {
        Base(final YangXPathMathMode mathMode, final YangExpr rootExpr, final String origStr) {
            super(mathMode, rootExpr, origStr);
        }

        @Override
        public YangQNameExpr interpretAsQName(final YangLiteralExpr expr) throws XPathExpressionException {
            return Utils.interpretAsQName(expr);
        }

        @Override
        InstanceIdentifierParser createInstanceIdentifierParser() {
            return new InstanceIdentifierParser.Base(getMathMode());
        }
    }

    static class Qualified extends AntlrYangXPathExpression implements QualifiedBound {
        final YangNamespaceContext namespaceContext;

        Qualified(final YangXPathMathMode mathMode, final YangExpr rootExpr,
            final String origStr, final YangNamespaceContext namespaceContext) {
            super(mathMode, rootExpr, origStr);
            this.namespaceContext = requireNonNull(namespaceContext);
        }

        YangNamespaceContext getNamespaceContext() {
            return namespaceContext;
        }

        @Override
        public YangQNameExpr interpretAsQName(final YangLiteralExpr expr) throws XPathExpressionException {
            return Utils.interpretAsQName(namespaceContext, expr);
        }

        @Override
        final InstanceIdentifierParser createInstanceIdentifierParser() {
            return new InstanceIdentifierParser.Qualified(getMathMode(), namespaceContext);
        }
    }

    static final class Unqualified extends Qualified implements UnqualifiedBound {
        private final QNameModule defaultNamespace;

        Unqualified(final YangXPathMathMode mathMode, final YangExpr rootExpr,
            final String origStr, final YangNamespaceContext namespaceContext, final QNameModule defaultNamespace) {
            super(mathMode, rootExpr, origStr, namespaceContext);
            this.defaultNamespace = requireNonNull(defaultNamespace);
        }

        @Override
        public Resolved interpretAsQName(final YangLiteralExpr expr) throws XPathExpressionException {
            return Utils.interpretAsQName(getNamespaceContext(), defaultNamespace, expr);
        }
    }

    private final YangXPathMathMode mathMode;
    private final YangExpr rootExpr;
    private final String origStr;

    AntlrYangXPathExpression(final YangXPathMathMode mathMode, final YangExpr rootExpr, final String origStr) {
        this.mathMode = requireNonNull(mathMode);
        this.rootExpr = requireNonNull(rootExpr);
        this.origStr = requireNonNull(origStr);
    }

    @Override
    public final YangXPathMathMode getMathMode() {
        return mathMode;
    }

    @Override
    public final YangExpr getRootExpr() {
        return rootExpr;
    }

    @Override
    public final YangLocationPath interpretAsInstanceIdentifier(final YangLiteralExpr expr)
            throws XPathExpressionException {
        return createInstanceIdentifierParser().interpretAsInstanceIdentifier(expr);
    }

    @Override
    public final String toString() {
        return origStr;
    }

    abstract InstanceIdentifierParser createInstanceIdentifierParser();
}
