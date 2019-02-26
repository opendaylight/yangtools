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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;

final class AntlrYangXPathExpression implements YangXPathExpression {
    private final YangNamespaceContext namespaceContext;
    private final YangExpr rootExpr;
    private final String origStr;

    AntlrYangXPathExpression(final YangNamespaceContext namespaceContext, final YangExpr rootExpr,
            final String origStr) {
        this.namespaceContext = requireNonNull(namespaceContext);
        this.rootExpr = requireNonNull(rootExpr);
        this.origStr = requireNonNull(origStr);
    }

    @Override
    public YangExpr getRootExpr() {
        return rootExpr;
    }

    @Override
    public YangQNameExpr interpretAsQName(final YangLiteralExpr expr) throws XPathExpressionException {
        // We are eagerly interpreting PrefixedName-compliant strings, hence they have a specific subclass
        final QName qname;
        if (expr instanceof QNameLiteralExpr) {
            qname = ((QNameLiteralExpr) expr).getQName();
        } else {
            try {
                // Deal with UnprefixedNames by interpreting them in implicit namespace
                qname = namespaceContext.createQName(expr.getLiteral());
            } catch (IllegalArgumentException | IllegalStateException e) {
                throw new XPathExpressionException(e);
            }
        }

        return YangQNameExpr.of(qname);
    }

    @Override
    public YangLocationPath interpretAsInstanceIdentifier(final YangLiteralExpr expr) throws XPathExpressionException {
        if (expr instanceof InstanceIdentifierLiteralExpr) {
            return YangLocationPath.of(true, ((InstanceIdentifierLiteralExpr)expr).getSteps());
        }
        throw new XPathExpressionException("Invalid instance-identifier " + expr);
    }

    @Override
    public String toString() {
        return origStr;
    }
}
