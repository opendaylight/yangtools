/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.guava;

import com.google.common.base.FinalizableReferenceQueue;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.objcache.ObjectCache;
import org.opendaylight.yangtools.objcache.spi.IObjectCacheFactory;

public final class GuavaObjectCacheFactory implements AutoCloseable, IObjectCacheFactory {
    private static final GuavaObjectCacheFactory INSTANCE = new GuavaObjectCacheFactory();
    private final FinalizableReferenceQueue  queue = new FinalizableReferenceQueue();
    private final ObjectCache cache;

    private GuavaObjectCacheFactory() {
        // FIXME: make this more dynamic using a spec
        this.cache = new GuavaObjectCache(queue);
    }

    @Override
    public ObjectCache getObjectCache(@Nonnull final Class<?> objClass) {
        return cache;
    }

    @Override
    public void close() {
        queue.close();
    }

    /**
     * Return a factory instance.
     *
     * @return A factory instance.
     */
    public static GuavaObjectCacheFactory getInstance() {
        return INSTANCE;
    }
}
