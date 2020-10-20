/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import jakarta.inject.Singleton;
import org.opendaylight.yangtools.odlext.parser.impl.OdlCodegenParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;

/**
 * A Dagger module binding {@code odl-codegen-extensions.yang} extension.
 *
 * @since 14.0.21
 */
@Module
public interface OdlCodegenModule {
    @Provides
    @Singleton
    @IntoSet
    static ParserExtension provideParserExtension() {
        return new OdlCodegenParserExtension();
    }
}
