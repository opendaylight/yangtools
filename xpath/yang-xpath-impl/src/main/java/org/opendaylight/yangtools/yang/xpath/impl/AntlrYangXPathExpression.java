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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr.Resolved;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;

abstract class AntlrYangXPathExpression implements YangXPathExpression {
    static final class Base extends AntlrYangXPathExpression {
        Base(final YangXPathMathMode mathMode, final YangVersion yangVersion, final YangExpr rootExpr,
                final String origStr) {
            super(mathMode, yangVersion, rootExpr, origStr);
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
        private final @Nullable YangNamespaceContext namespaceContext;

        Qualified(final YangXPathMathMode mathMode, final YangVersion yangVersion, final YangExpr rootExpr,
                final String origStr, final @Nullable YangNamespaceContext namespaceContext) {
            super(mathMode, yangVersion, rootExpr, origStr);
            this.namespaceContext = namespaceContext;
        }

        final YangNamespaceContext namespaceContext() throws XPathExpressionException {
            final YangNamespaceContext local = namespaceContext;
            if (local == null) {
                throw new XPathExpressionException("Expression does not have a legal literal member");
            }
            return local;
        }

        @Override
        public YangQNameExpr interpretAsQName(final YangLiteralExpr expr) throws XPathExpressionException {
            return Utils.interpretAsQName(namespaceContext(), expr);
        }

        @Override
        InstanceIdentifierParser createInstanceIdentifierParser() throws XPathExpressionException {
            return new InstanceIdentifierParser.Qualified(getMathMode(), namespaceContext());
        }
    }

    static final class Unqualified extends Qualified implements UnqualifiedBound {
        private final QNameModule defaultNamespace;

        Unqualified(final YangXPathMathMode mathMode, final YangVersion yangVersion, final YangExpr rootExpr,
                final String origStr, final @Nullable YangNamespaceContext namespaceContext,
                final QNameModule defaultNamespace) {
            super(mathMode, yangVersion, rootExpr, origStr, namespaceContext);
            this.defaultNamespace = requireNonNull(defaultNamespace);
        }

        @Override
        public Resolved interpretAsQName(final YangLiteralExpr expr) throws XPathExpressionException {
            return Utils.interpretAsQName(namespaceContext(), defaultNamespace, expr);
        }

        @Override
        InstanceIdentifierParser createInstanceIdentifierParser() throws XPathExpressionException {
            return new InstanceIdentifierParser.Unqualified(getMathMode(), namespaceContext(), defaultNamespace);
        }
    }

    private final YangXPathMathMode mathMode;
    private final YangVersion yangVersion;
    private final YangExpr rootExpr;
    private final String origStr;

    AntlrYangXPathExpression(final YangXPathMathMode mathMode, final YangVersion yangVersion, final YangExpr rootExpr,
            final String origStr) {
        this.mathMode = requireNonNull(mathMode);
        this.yangVersion = requireNonNull(yangVersion);
        this.rootExpr = requireNonNull(rootExpr);
        this.origStr = requireNonNull(origStr);
    }

    @Override
    public final YangXPathMathMode getMathMode() {
        return mathMode;
    }

    @Override
    public final YangVersion getYangVersion() {
        return yangVersion;
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

    abstract InstanceIdentifierParser createInstanceIdentifierParser() throws XPathExpressionException;
}
