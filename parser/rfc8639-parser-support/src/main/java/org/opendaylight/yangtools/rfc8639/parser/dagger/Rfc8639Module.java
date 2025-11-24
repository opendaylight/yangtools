/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.parser.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc8639.parser.impl.Rfc8639ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;

/**
 * A Dagger module binding RFC8639 extension.
 *
 * @since 14.0.21
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface Rfc8639Module {
    @Provides
    @Singleton
    @IntoSet
    static ParserExtension provideParserExtension() {
        return new Rfc8639ParserExtension();
    }
}
