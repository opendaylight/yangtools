/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import javax.xml.xpath.XPathExpressionException;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.QualifiedQName;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr.Resolved;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr.Unresolved;

/**
 * Various simplistic utilities shared across classes.
 */
final class Utils {
    private Utils() {

    }

    static Unresolved interpretAsQName(final YangLiteralExpr expr) throws XPathExpressionException {
        final String text = expr.getLiteral();
        final int colon = text.indexOf(':');

        final AbstractQName qname;
        try {
           qname = colon != -1 ? QualifiedQName.of(text.substring(0, colon), text.substring(colon + 1))
                    : UnqualifiedQName.of(text);
        } catch (IllegalArgumentException e) {
            throw wrapException(e, "Cannot interpret %s as a QName", expr);
        }

        return YangQNameExpr.of(qname);
    }

    static YangQNameExpr interpretAsQName(final YangNamespaceContext namespaceContext, final YangLiteralExpr expr)
            throws XPathExpressionException {
        final String text = expr.getLiteral();
        final int colon = text.indexOf(':');
        try {
            if (colon == -1) {
                return YangQNameExpr.of(UnqualifiedQName.of(text));
            }

            return YangQNameExpr.of(namespaceContext.createQName(text.substring(0, colon), text.substring(colon + 1)));
        } catch (IllegalArgumentException e) {
            throw wrapException(e, "Cannot interpret %s as a QName", expr);
        }
    }

    static Resolved interpretAsQName(final YangNamespaceContext namespaceContext,
            final QNameModule defaultNamespace, final YangLiteralExpr expr) throws XPathExpressionException {
        final String text = expr.getLiteral();
        final int colon = text.indexOf(':');
        final QName qname;

        try {
            qname = colon == -1 ? QName.create(defaultNamespace, text)
                    : namespaceContext.createQName(text.substring(0, colon), text.substring(colon + 1));
        } catch (IllegalArgumentException e) {
            throw wrapException(e, "Cannot interpret %s as a QName", expr);
        }

        return YangQNameExpr.of(qname);
    }

    static XPathExpressionException wrapException(final @Nullable Throwable cause, final String format,
            final Object... args) {
        final XPathExpressionException ret = new XPathExpressionException(String.format(format, args));
        ret.initCause(cause);
        return ret;
    }
}
