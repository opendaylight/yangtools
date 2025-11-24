/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.ri.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.DefaultYangParserFactory;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * A Dagger module providing {@link YangParserFactory}.
 *
 * @since 14.0.21
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface YangParserFactoryModule {
    @Provides
    @Singleton
    static YangParserFactory provideParserFactory(final YangXPathParserFactory xpathFactory,
            final Set<ParserExtension> extensions) {
        return new DefaultYangParserFactory(xpathFactory, extensions);
    }
}
