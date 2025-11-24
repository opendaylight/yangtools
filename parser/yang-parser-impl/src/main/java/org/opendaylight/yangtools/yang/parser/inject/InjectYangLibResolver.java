/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.inject;

import java.io.IOException;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceRepresentation;
import org.opendaylight.yangtools.yang.parser.api.YangLibModuleSet;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.impl.DefaultYangLibResolver;
import org.opendaylight.yangtools.yang.parser.impl.dagger.YangLibResolverModule;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * Reference {@link YangLibResolver} implementation.
 *
 * @since 14.0.20
 * @deprecated Use {@link YangLibResolverModule#provideYangLibResolver(YangXPathParserFactory, java.util.Set)} instead.
 */
@Singleton
@SuppressWarnings("exports")
@Deprecated(since = "14.0.21", forRemoval = true)
public final class InjectYangLibResolver implements YangLibResolver {
    private final @NonNull DefaultYangLibResolver delegate;

    @Inject
    public InjectYangLibResolver(final YangXPathParserFactory xpathFactory) {
        delegate = new DefaultYangLibResolver(xpathFactory);
    }

    @Override
    public Collection<Class<? extends SourceRepresentation>> supportedSourceRepresentations() {
        return delegate.supportedSourceRepresentations();
    }

    @Override
    public EffectiveModelContext resolveModuleSet(final YangLibModuleSet moduleSet)
            throws IOException, YangParserException {
        return delegate.resolveModuleSet(moduleSet);
    }
}
