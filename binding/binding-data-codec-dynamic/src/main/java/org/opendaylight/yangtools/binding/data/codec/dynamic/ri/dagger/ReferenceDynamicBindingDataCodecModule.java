/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.dynamic.ri.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.opendaylight.yangtools.binding.data.codec.dynamic.DynamicBindingDataCodec;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingCodecContext;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;

/**
 * A Dagger module providing reference {@link DynamicBindingDataCodec} implementation.
 */
@Module
public interface ReferenceDynamicBindingDataCodecModule {
    @Provides
    @Singleton
    static DynamicBindingDataCodec provideDynamicBindingDataCodec(final BindingRuntimeContext context) {
        return new BindingCodecContext(context);
    }
}
