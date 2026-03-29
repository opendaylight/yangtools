/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.binding.generator.BindingGenerator;
import org.opendaylight.yangtools.binding.generator.impl.DefaultBindingGenerator;

/**
 * A Dagger module exposing the {@link BindingGenerator} implementation.
 *
 * @since 15.0.3
 */
@Module
@NonNullByDefault
@SuppressWarnings("exports")
public interface BindingGeneratorModule {
    @Provides
    @Singleton
    static BindingGenerator provideBindingGenerator() {
        return new DefaultBindingGenerator();
    }
}
