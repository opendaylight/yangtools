/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.dynamic.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.data.codec.dynamic.BindingDataCodecFactory;
import org.opendaylight.yangtools.binding.data.codec.impl.SimpleBindingDOMCodecFactory;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecFactory;

/**
 * A Dagger module providing reference {@link BindingDataCodecFactory} implementation.
 *
 * @since 14.0.21
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface BindingDataCodecFactoryModule {
    @Provides
    @Singleton
    static BindingDataCodecFactory provideBindingDataCodecFactory() {
        return new SimpleBindingDOMCodecFactory();
    }

    @Provides
    @Singleton
    @Deprecated(since = "14.0.21", forRemoval = true)
    static BindingDOMCodecFactory provideBindingDOMCodecFactory() {
        return new SimpleBindingDOMCodecFactory();
    }
}
