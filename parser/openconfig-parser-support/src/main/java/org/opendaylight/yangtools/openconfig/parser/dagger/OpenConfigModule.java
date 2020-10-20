/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import jakarta.inject.Singleton;
import org.opendaylight.yangtools.openconfig.parser.impl.OpenConfigParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;

/**
 * A Dagger module binding {@code openconfig-extensions.yang} extension.
 *
 * @since 14.0.21
 */
@Module
public interface OpenConfigModule {
    @Provides
    @Singleton
    @IntoSet
    static ParserExtension provideParserExtension() {
        return new OpenConfigParserExtension();
    }
}
