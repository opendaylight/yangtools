/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.ri.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathParserFactory;
import org.opendaylight.yangtools.yang.xpath.impl.AntlrXPathParserFactory;

/**
 * Module providing reference {@link YangXPathParserFactory}.
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface ReferenceYangXPathParserFactoryModule {
    @Provides
    @Singleton
    static YangXPathParserFactory provideYangXPathParserFactory() {
        return new AntlrXPathParserFactory();
    }
}
