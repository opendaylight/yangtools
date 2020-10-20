/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.generator.impl.DefaultBindingRuntimeGenerator;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeGenerator;

/**
 * A Dagger module exposing the {@link BindingRuntimeGenerator} implementation.
 */
@Module
@NonNullByDefault
public interface BindingRuntimeGeneratorModule {
    @Provides
    @Singleton
    static BindingRuntimeGenerator provideBindingRuntimeGenerator() {
        return new DefaultBindingRuntimeGenerator();
    }
}
