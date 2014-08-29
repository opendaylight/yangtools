/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

import org.opendaylight.yangtools.objcache.impl.StaticObjectCacheBinder;
import org.opendaylight.yangtools.objcache.spi.IObjectCacheFactory;
import org.opendaylight.yangtools.objcache.spi.NoopObjectCacheBinder;

/**
 * Point of entry for acquiring an {@link ObjectCache} instance.
 */
public final class ObjectCacheFactory {
    private static volatile IObjectCacheFactory factory;

    @GuardedBy("this")
    private static synchronized IObjectCacheFactory initialize() {
        // Double-check under lock
        IObjectCacheFactory f = factory;
        if (f != null) {
            return f;
        }

        try {
            f = StaticObjectCacheBinder.getInstance().getProductCacheFactory();
            factory = f;
        } catch (NoClassDefFoundError e) {
            f = NoopObjectCacheBinder.INSTANCE.getProductCacheFactory();
        }

        return f;
    }

    public static synchronized void reset() {
        factory = null;
    }

    /**
     * Get an ObjectCache for caching a particular object class. Note
     * that it may be shared for multiple classes.
     *
     * @param objClass Class of objects which are to be cached
     * @return Object cache instance.
     */
    public static ObjectCache getObjectCache(@Nonnull final Class<?> objClass) {
        IObjectCacheFactory f = factory;
        if (f == null) {
            f = initialize();
        }

        return f.getObjectCache(Preconditions.checkNotNull(objClass));
    }
}
