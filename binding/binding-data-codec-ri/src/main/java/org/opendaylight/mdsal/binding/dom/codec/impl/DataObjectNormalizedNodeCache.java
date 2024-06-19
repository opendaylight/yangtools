/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A cache of NormalizedNodes corresponding to a particular DataObject instantiation.
 */
final class DataObjectNormalizedNodeCache
        extends AbstractBindingNormalizedNodeCache<DataObject, DataContainerCodecContext<?, ?, ?>> {
    private final AbstractBindingNormalizedNodeCacheHolder cacheHolder;

    DataObjectNormalizedNodeCache(final AbstractBindingNormalizedNodeCacheHolder cacheHolder,
            final DataContainerCodecContext<?, ?, ?> rootContext) {
        super(rootContext);
        this.cacheHolder = requireNonNull(cacheHolder, "cacheHolder");
    }

    @Override
    public NormalizedNode load(final DataObject key) {
        return CachingNormalizedNodeSerializer.serializeUsingStreamWriter(cacheHolder, rootContext(), key);
    }
}
