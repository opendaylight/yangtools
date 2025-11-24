/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser.dagger;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc7952.parser.impl.Rfc7952ParserExtension;
import org.opendaylight.yangtools.yang.parser.spi.ParserExtension;

/**
 * A Dagger module binding RFC7952 extension.
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface Rfc7952Module {
    @Provides
    @Singleton
    @IntoSet
    static ParserExtension provideParserExtension() {
        return new Rfc7952ParserExtension();
    }
}
