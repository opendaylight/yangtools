/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.ri.DefaultYangLibResolver;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * A Dagger module providing {@link YangLibResolver}.
 *
 * @since 14.0.21
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface YangLibResolverModule {
    @Provides
    @Singleton
    static YangLibResolver provideYangLibResolver(final YangXPathParserFactory xpathFactory,
            final Set<ParserExtension> extensions) {
        return new DefaultYangLibResolver(xpathFactory, extensions);
    }
}
