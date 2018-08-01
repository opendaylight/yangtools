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
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;

final class AntlrYangXPathExpression implements YangXPathExpression {
    private final QNameSupport qnameSupport;
    private final YangExpr rootExpr;
    private final String origStr;

    AntlrYangXPathExpression(final QNameSupport qnameSupport, final YangExpr rootExpr, final String origStr) {
        this.qnameSupport = requireNonNull(qnameSupport);
        this.rootExpr = requireNonNull(rootExpr);
        this.origStr = requireNonNull(origStr);
    }

    @Override
    public YangExpr getRootExpr() {
        return rootExpr;
    }

    @Override
    public QName interpretAsQName(final YangLiteralExpr expr) throws XPathExpressionException {
        // We are eagerly interpreting PrefixedName-compliant strings, hence they have a specific subclass
        if (expr instanceof QNameLiteralExpr) {
            return ((QNameLiteralExpr) expr).getQName();
        }

        try {
            // Deal with UnprefixedNames by interpreting them in implicit namespace
            return qnameSupport.createQName(expr.getLiteral());
        } catch (IllegalArgumentException e) {
            throw new XPathExpressionException(e);
        }
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
