/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.source.ir.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRToTextSourceTransformer;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.source.ir.DefaultYangIRToTextSourceTransformer;
import org.opendaylight.yangtools.yang.source.ir.DefaultYangTextToIRSourceTransformer;

/**
 * Module providing reference {@link YangIRToTextSourceTransformer}.
 *
 * @since 15.0.0
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface YangIRSourceModule {
    @Provides
    @Singleton
    static YangTextToIRSourceTransformer provideTextToIR() {
        return new DefaultYangTextToIRSourceTransformer();
    }

    @Provides
    @Singleton
    static YangIRToTextSourceTransformer provideIRTText() {
        return new DefaultYangIRToTextSourceTransformer();
    }
}
