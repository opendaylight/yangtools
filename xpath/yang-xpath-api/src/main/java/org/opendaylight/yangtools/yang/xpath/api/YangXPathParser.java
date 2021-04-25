/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import javax.xml.xpath.XPathExpressionException;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;

/**
 * Interface for converting a String into a {@link YangXPathExpression}. Implementations of this interface are expected
 * to be NOT thread-safe.
 *
 * @author Robert Varga
 */
@Beta
public interface YangXPathParser {
    /**
     * A {@link YangXPathParser} bound to a {@link YangNamespaceContext}, producing Qualified-bound Expressions.
     */
    interface QualifiedBound extends YangXPathParser {
        @Override
        YangXPathExpression.QualifiedBound parseExpression(String xpath) throws XPathExpressionException;
    }

    /**
     * A {@link YangXPathParser} bound to a {@link YangNamespaceContext} and a default namespace, producing
     * Unqualified-bound Expressions.
     */
    interface UnqualifiedBound extends QualifiedBound {
        @Override
        YangXPathExpression.UnqualifiedBound parseExpression(String xpath) throws XPathExpressionException;
    }

    /**
     * Parse a string containing an XPath expression.
     *
     * @param xpath XPath expression string
     * @return A parsed {@link YangXPathExpression}
     * @throws NullPointerException if {@code xpath} is null
     * @throws XPathExpressionException when the expression cannot be parsed
     */
    YangXPathExpression parseExpression(String xpath) throws XPathExpressionException;
}
