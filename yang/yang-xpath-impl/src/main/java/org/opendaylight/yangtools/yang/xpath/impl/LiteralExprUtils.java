/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import javax.xml.xpath.XPathExpressionException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;

/**
 * Utilities for interpreting {@link YangLiteralExpr}s as {@link YangQNameExpr}s and {@link YangLocationPath}s.
 */
final class LiteralExprUtils {
    private LiteralExprUtils() {

    }

    static YangQNameExpr interpretAsQName(final YangNamespaceContext namespaceContext, final YangLiteralExpr expr)
            throws XPathExpressionException {
        final String text = expr.getLiteral();
        final int colon = text.indexOf(':');
        final QName qname;
        if (colon != -1) {
            try {
                qname = namespaceContext.createQName(text.substring(0, colon), text.substring(colon + 1));
            } catch (IllegalArgumentException e) {
                throw new XPathExpressionException(e);
            }
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
}
