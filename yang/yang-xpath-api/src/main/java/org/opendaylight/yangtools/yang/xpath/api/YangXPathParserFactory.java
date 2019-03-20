/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;

/**
 * Factory for creating {@link YangXPathParser}s. Implementations of this interface are expected to be thread-safe.
 *
 * @author Robert Varga
 */
@Beta
public interface YangXPathParserFactory {
    /**
     * Return a {@link YangXPathParser} compliant with {@link YangXPathMathMode#IEEE754}. Returned parser will not
     * perform any namespace binding.
     *
     * @param namespaceContext Prefix-to-namespace resolver
     * @return An XPathParser
     */
    default YangXPathParser newParser() {
        return newParser(YangXPathMathMode.IEEE754);
    }

    /**
     * Return a {@link YangXPathParser} compliant with {@link YangXPathMathMode}. Returned parser will not perform any
     * namespace binding.
     *
     * @param mathMode Requested XPath number compliance
     * @return An XPathParser
     * @throws NullPointerException if {@code mathMode} is null
     */
    YangXPathParser newParser(YangXPathMathMode mathMode);

    /**
     * Return a {@link YangXPathParser} compliant with {@link YangXPathMathMode#IEEE754}. Returned parser will bind
     * qualified node identifiers to {@link QName}s.
     *
     * @param namespaceContext Prefix-to-namespace resolver, used to bind qualified node identifiers
     * @return An XPathParser
     * @throws NullPointerException if {@code namespaceContext} is null
     */
    default YangXPathParser.QualifiedBound newParser(final YangNamespaceContext namespaceContext) {
        return newParser(YangXPathMathMode.IEEE754, namespaceContext);
    }

    /**
     * Return a {@link YangXPathParser} compliant with {@link YangXPathMathMode}. Returned parser will bind qualified
     * node identifiers to {@link QName}s.
     *
     * @param mathMode Requested XPath number compliance
     * @param namespaceContext Prefix-to-namespace resolver, used to bind qualified node identifiers
     * @return An XPathParser
     * @throws NullPointerException if any argument is null
     */
    YangXPathParser.QualifiedBound newParser(YangXPathMathMode mathMode, YangNamespaceContext namespaceContext);

    /**
     * Return a {@link YangXPathParser} compliant with {@link YangXPathMathMode#IEEE754}. Returned parser will bind
     * qualified and unqualified node identifiers to {@link QName}s.
     *
     * @param namespaceContext Prefix-to-namespace resolver, used to bind qualified node identifiers
     * @param defaultNamespace Default namespace, used to bind unqualified node identifiers
     * @return An XPathParser
     * @throws NullPointerException if any argument is null
     */
    default YangXPathParser.UnqualifiedBound newParser(final YangNamespaceContext namespaceContext,
            final QNameModule defaultNamespace) {
        return newParser(YangXPathMathMode.IEEE754, namespaceContext, defaultNamespace);
    }

    /**
     * Return a {@link YangXPathParser} compliant with {@link YangXPathMathMode}. Returned parser will bind qualified
     * and unqualified node identifiers to {@link QName}s.
     *
     * @param mathMode Requested XPath number compliance
     * @param namespaceContext Prefix-to-namespace resolver, used to bind qualified node identifiers
     * @param defaultNamespace Default namespace, used to bind unqualified node identifiers
     * @return An XPathParser
     * @throws NullPointerException if any argument is null
     */
    YangXPathParser.UnqualifiedBound newParser(YangXPathMathMode mathMode, YangNamespaceContext namespaceContext,
            QNameModule defaultNamespace);
}
