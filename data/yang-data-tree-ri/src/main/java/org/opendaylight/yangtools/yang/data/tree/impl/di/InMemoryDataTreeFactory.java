/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl.di;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.tree.impl.ReferenceDataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.ri.dagger.ReferenceDataTreeFactoryModule;

/**
 * A factory for creating in-memory data trees.
 *
 * @deprecated Use {@link ReferenceDataTreeFactoryModule#provideDataTreeFactory()} instead.
 */
@Singleton
@NonNullByDefault
@Deprecated(since = "14.0.21", forRemoval = true)
public final class InMemoryDataTreeFactory extends ReferenceDataTreeFactory {
    @Inject
    public InMemoryDataTreeFactory() {
        // nothing else
    }
}
