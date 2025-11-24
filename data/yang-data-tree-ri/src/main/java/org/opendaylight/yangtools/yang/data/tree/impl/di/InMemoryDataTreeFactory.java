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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.tree.api.DataTree;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeFactory;
import org.opendaylight.yangtools.yang.data.tree.api.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.tree.dagger.ReferenceDataTreeFactoryModule;
import org.opendaylight.yangtools.yang.data.tree.impl.ReferenceDataTreeFactory;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * A factory for creating in-memory data trees.
 *
 * @deprecated Use {@link ReferenceDataTreeFactoryModule#provideDataTreeFactory()} instead.
 */
@Singleton
@Deprecated(since = "14.0.21", forRemoval = true)
@SuppressWarnings("exports")
public final class InMemoryDataTreeFactory implements DataTreeFactory {
    private final @NonNull ReferenceDataTreeFactory delegate = new ReferenceDataTreeFactory();

    @Inject
    public InMemoryDataTreeFactory() {
        // nothing else
    }

    @Override
    @Deprecated(since = "14.0.21", forRemoval = true)
    public DataTree create(final DataTreeConfiguration treeConfig) {
        return delegate.create(treeConfig);
    }

    @Override
    public DataTree create(final DataTreeConfiguration treeConfig, final EffectiveModelContext initialSchemaContext) {
        return delegate.create(treeConfig, initialSchemaContext);
    }

    @Override
    public DataTree create(final DataTreeConfiguration treeConfig, final EffectiveModelContext initialSchemaContext,
            final DistinctNodeContainer<?, ?> initialRoot) throws DataValidationFailedException {
        return delegate.create(treeConfig, initialSchemaContext, initialRoot);
    }
}
