/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

import org.opendaylight.yangtools.objcache.ObjectCache;

public final class NoopObjectCacheBinder extends AbstractObjectCacheBinder {
    public static final NoopObjectCacheBinder INSTANCE = new NoopObjectCacheBinder();

    private  NoopObjectCacheBinder() {
        super(new IObjectCacheFactory() {
            @Override
            public ObjectCache getObjectCache(final Class<?> objClass) {
                return NoopObjectCache.getInstance();
            }
        });
    }
}
