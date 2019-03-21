/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;

/**
 * Factory for creating {@link YangXPathParser}s. Implementations of this interface are expected to be thread-safe.
 *
 * @author Robert Varga
 */
@Beta
public interface YangXPathParserFactory {
    /**
     * Return a {@link YangXPathParser} compliant with {@link YangXPathMathMode#IEEE754}.
     *
     * @param namespaceContext Prefix-to-namespace resolver
     * @return An XPathParser
     * @throws NullPointerException if {@code namespaceContext} is null
     */
    default YangXPathParser newParser(final YangNamespaceContext namespaceContext) {
        return newParser(namespaceContext, YangXPathMathMode.IEEE754);
    }

    /**
     * Return a {@link YangXPathParser} compliant with {@link YangXPathMathMode}.
     *
     * @param namespaceContext Prefix-to-namespace resolver
     * @param mathMode Requested XPath number compliance
     * @return An XPathParser
     * @throws NullPointerException if any argument is null
     */
    YangXPathParser newParser(YangNamespaceContext namespaceContext, YangXPathMathMode mathMode);
}
