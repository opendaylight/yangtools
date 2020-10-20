/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.inject;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import java.util.Set;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.DefaultYangParserFactory;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;

/**
 * A Dagger module binding default YANG parser.
 */
@Module
public abstract class YangParserFactoryModule {
    @Binds
    public abstract YangLibResolver yangLibResolver(InjectYangLibResolver resolver);

    @Provides
    public static YangParserFactory parserFactory(final YangXPathParserFactory xpathFactory,
            Set<ParserExtension> extensions) {
        return new DefaultYangParserFactory(xpathFactory);
    }
}
