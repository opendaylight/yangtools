/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.ProductAwareBuilder;
import org.opendaylight.yangtools.objcache.ObjectCache;

/**
 * No-operation implementation of an Object Cache. This implementation
 * does not do any caching, so it only returns the request object.
 */
public final class NoopObjectCache implements ObjectCache {
    private static final NoopObjectCache INSTANCE = new NoopObjectCache();

    private NoopObjectCache() {

    }

    /**
     * Get the cache instance. Since the cache does not have any state,
     * this method always returns a singleton instance.
     *
     * @return Cache instance.
     */
    public static NoopObjectCache getInstance() {
        return INSTANCE;
    }

    @Override
    public <T> T getReference(final T object) {
        return object;
    }

    @Override
    public <B extends ProductAwareBuilder<P>, P> P getProduct(@Nonnull final B builder) {
        return builder.build();
    }
}
