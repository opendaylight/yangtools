/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.ri.dagger;

import dagger.Component;
import jakarta.inject.Singleton;
import org.opendaylight.yangtools.odlext.parser.dagger.OdlCodegenModule;
import org.opendaylight.yangtools.odlext.parser.dagger.YangExtModule;
import org.opendaylight.yangtools.openconfig.parser.dagger.OpenConfigModule;
import org.opendaylight.yangtools.rfc6241.parser.dagger.Rfc6241Module;
import org.opendaylight.yangtools.rfc6536.parser.dagger.Rfc6536Module;
import org.opendaylight.yangtools.rfc6643.parser.dagger.Rfc6643Module;
import org.opendaylight.yangtools.rfc7952.parser.dagger.Rfc7952Module;
import org.opendaylight.yangtools.rfc8040.parser.dagger.Rfc8040Module;
import org.opendaylight.yangtools.rfc8528.parser.dagger.Rfc8528Module;
import org.opendaylight.yangtools.rfc8639.parser.dagger.Rfc8639Module;
import org.opendaylight.yangtools.rfc8819.parser.dagger.Rfc8819Module;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.xpath.dagger.YangXPathParserFactoryModule;

/**
 * A component exposing {@link YangParserFactory} and {@link YangLibResolver}.
 *
 * @since 14.0.21
 */
@Singleton
@Component(
    modules = {
        YangXPathParserFactoryModule.class,
        YangParserFactoryModule.class,
        YangLibResolverModule.class,
        Rfc6241Module.class,
        Rfc6536Module.class,
        Rfc6643Module.class,
        Rfc7952Module.class,
        Rfc8040Module.class,
        Rfc8528Module.class,
        Rfc8639Module.class,
        Rfc8819Module.class,
        OdlCodegenModule.class,
        YangExtModule.class,
        OpenConfigModule.class,
    })
public interface YangParserComponent {

    YangParserFactory parserFactory();

    YangLibResolver yangLibResolver();
}
