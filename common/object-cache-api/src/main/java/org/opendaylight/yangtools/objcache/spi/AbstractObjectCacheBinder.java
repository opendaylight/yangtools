/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;

public abstract class AbstractObjectCacheBinder implements ObjectCacheFactoryBinder {
    private final IObjectCacheFactory factory;

    protected AbstractObjectCacheBinder(@Nonnull final IObjectCacheFactory factory) {
        this.factory = requireNonNull(factory);
    }

    @Override
    public final IObjectCacheFactory getProductCacheFactory() {
        return factory;
    }
}
