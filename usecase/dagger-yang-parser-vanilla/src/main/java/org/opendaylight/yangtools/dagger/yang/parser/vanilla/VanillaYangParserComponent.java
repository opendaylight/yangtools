/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.dagger.yang.parser.vanilla;

import dagger.Component;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.dagger.EmptyParserExtensionsModule;
import org.opendaylight.yangtools.yang.parser.dagger.YangLibResolverModule;
import org.opendaylight.yangtools.yang.parser.dagger.YangParserFactoryModule;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;
import org.opendaylight.yangtools.yang.xpath.dagger.YangXPathParserFactoryModule;
import org.opendaylight.yangtools.yin.source.dom.dagger.YinDOMSourceModule;

/**
 * A {@link Component} providing a plain RFC6020/RFC7950 {@link YangLibResolver}.
 * @since 15.0.0
 */
@Singleton
@Component(
    modules = {
        YangIRSourceModule.class,
        YinDOMSourceModule.class,
        YangXPathParserFactoryModule.class,
        YangLibResolverModule.class,
        YangParserFactoryModule.class,
        EmptyParserExtensionsModule.class,
    })
@NonNullByDefault
public interface VanillaYangParserComponent {
    /**
     * {@return the YangLibResolver}
     */
    YangLibResolver yangLibResolver();

    /**
     * {@return the YangParserFactory}
     */
    YangParserFactory parserFactory();
}
