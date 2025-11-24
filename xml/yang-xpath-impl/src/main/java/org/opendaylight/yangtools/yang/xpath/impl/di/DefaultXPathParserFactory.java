/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl.di;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangNamespaceContext;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathMathMode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser.QualifiedBound;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParser.UnqualifiedBound;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.opendaylight.yangtools.yang.xpath.dagger.YangXPathParserFactoryModule;
import org.opendaylight.yangtools.yang.xpath.impl.AntlrXPathParserFactory;

/**
 * Default implementation of {@link YangXPathParserFactory} for {@code javax.inject}-based dependency injection
 * frameworks.
 *
 * @deprecated Use {@link YangXPathParserFactoryModule#provideYangXPathParserFactory()} instead..
 */
@Singleton
@Deprecated(forRemoval = true, since = "14.0.21")
public final class DefaultXPathParserFactory implements YangXPathParserFactory {
    private final AntlrXPathParserFactory delegate = new AntlrXPathParserFactory();

    /**
     * Construct a parser factory.
     */
    @Inject
    public DefaultXPathParserFactory() {
        // Noop
    }

    @Override
    public YangXPathParser newParser(YangXPathMathMode mathMode) {
        return delegate.newParser(mathMode);
    }

    @Override
    public QualifiedBound newParser(final YangXPathMathMode mathMode, final YangNamespaceContext namespaceContext) {
        return delegate.newParser(mathMode, namespaceContext);
    }

    @Override
    public UnqualifiedBound newParser(final YangXPathMathMode mathMode, final YangNamespaceContext namespaceContext,
            final QNameModule defaultNamespace) {
        return delegate.newParser(mathMode, namespaceContext, defaultNamespace);
    }
}
