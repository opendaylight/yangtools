package org.opendaylight.yangtools.yang.xpath.impl;

/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
import static java.util.Objects.requireNonNull;

import javax.xml.xpath.XPathExpressionException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Absolute;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression;

final class AntlrYangXPathExpression implements YangXPathExpression {
    private final QNameModule implicitNamespace;
    private final YangExpr rootExpr;
    private final String origStr;

    AntlrYangXPathExpression(final QNameModule implicitNamespace, final YangExpr rootExpr, final String origStr) {
        this.implicitNamespace = requireNonNull(implicitNamespace);
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
            return QName.create(implicitNamespace, expr.getLiteral());
        } catch (IllegalArgumentException e) {
            throw new XPathExpressionException("Invalid QName " + expr);
        }
    }

    @Override
    public Absolute interpretAsInstanceIdentifier(final YangLiteralExpr expr) throws XPathExpressionException {
        if (expr instanceof InstanceIdentifierLiteralExpr) {
            return ((InstanceIdentifierLiteralExpr)expr).getPath();
        }
        throw new XPathExpressionException("Invalid instance-identifier " + expr);
    }

    @Override
    public String toString() {
        return origStr;
    }
}
