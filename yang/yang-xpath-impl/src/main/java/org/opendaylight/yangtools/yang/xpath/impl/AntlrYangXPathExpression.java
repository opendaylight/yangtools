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
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;

final class AntlrYangXPathExpression implements YangXPathExpression {
    private final YangNamespaceContext namespaceContext;
    private final YangXPathMathMode mathMode;
    private final YangExpr rootExpr;
    private final String origStr;

    AntlrYangXPathExpression(final YangNamespaceContext namespaceContext, final YangXPathMathMode mathMode,
            final YangExpr rootExpr, final String origStr) {
        this.namespaceContext = requireNonNull(namespaceContext);
        this.mathMode = requireNonNull(mathMode);
        this.rootExpr = requireNonNull(rootExpr);
        this.origStr = requireNonNull(origStr);
    }

    @Override
    public YangXPathMathMode getMathMode() {
        return mathMode;
    }

    @Override
    public YangExpr getRootExpr() {
        return rootExpr;
    }

    @Override
    public YangQNameExpr interpretAsQName(final YangLiteralExpr expr) throws XPathExpressionException {
        return Utils.interpretAsQName(namespaceContext, expr);
    }

    @Override
    public YangLocationPath interpretAsInstanceIdentifier(final YangLiteralExpr expr) throws XPathExpressionException {
        return new InstanceIdentifierParser(namespaceContext, mathMode).interpretAsInstanceIdentifier(expr);
    }

    @Override
    public String toString() {
        return origStr;
    }
}
