/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.parser.api.YangLibModuleSet;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.dagger.YangLibResolverModule;
import org.opendaylight.yangtools.yang.parser.inject.InjectYangLibResolver;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * Reference {@link YangLibResolver} implementation.
 */
@Deprecated(since = "14.0.21", forRemoval = true)
public sealed class DefaultYangLibResolver implements YangLibResolver permits InjectYangLibResolver {
    private final @NonNull YangLibResolver delegate;

    /**
     * Default constructor for {@link ServiceLoader} instantiation.
     */
    public DefaultYangLibResolver() {
        this(ServiceLoader.load(YangXPathParserFactory.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("No YangXPathParserFactory found")));
    }

    public DefaultYangLibResolver(final YangXPathParserFactory xpathFactory) {
        this(xpathFactory, ServiceLoader.load(ParserExtension.class).stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toUnmodifiableSet()));
    }

    public DefaultYangLibResolver(final YangXPathParserFactory xpathFactory, final Set<ParserExtension> extensions) {
        delegate = YangLibResolverModule.provideYangLibResolver(xpathFactory, extensions);
    }

    @Override
    public final Collection<Class<? extends SourceRepresentation>> supportedSourceRepresentations() {
        return delegate.supportedSourceRepresentations();
    }

    @Override
    public final EffectiveModelContext resolveModuleSet(final YangLibModuleSet moduleSet)
            throws IOException, YangParserException {
        return delegate.resolveModuleSet(moduleSet);
    }
}
