/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.inject;

import dagger.Component;
import javax.inject.Singleton;
import org.opendaylight.yangtools.rfc6241.parser.inject.Rfc6241ParserExtensionModule;
import org.opendaylight.yangtools.yang.parser.api.YangLibResolver;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.xpath.impl.di.AntlrYangXPathParserModule;

/**
 * A component exposing {@link YangParserFactory} and {@link YangLibResolver}.
 */
@Component(
    modules = {
        AntlrYangXPathParserModule.class,
        YangParserFactoryModule.class,
        Rfc6241ParserExtensionModule.class,
    })
@Singleton
public interface YangParserFactoryComponent {

    YangParserFactory parserFactory();

    YangLibResolver yangLibResolver();
}
