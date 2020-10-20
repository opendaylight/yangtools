/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.ri.dagger;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.impl.di.InMemoryDataTreeFactory;

/**
 * A Dagger module providing the reference {@link DataTreeFactory} implementation.
 */
@Module
@NonNullByDefault
public interface ReferenceDataTreeFactoryModule {
    @Provides
    @Singleton
    static DataTreeFactory provideDataTreeFactory() {
        return new InMemoryDataTreeFactory();
    }
}
