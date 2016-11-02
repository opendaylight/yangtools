/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.impl;

import org.opendaylight.yangtools.objcache.spi.AbstractObjectCacheBinder;
import org.opendaylight.yangtools.objcache.spi.NoopObjectCache;

public final class StaticObjectCacheBinder extends AbstractObjectCacheBinder {
    private static final StaticObjectCacheBinder INSTANCE = new StaticObjectCacheBinder();

    private StaticObjectCacheBinder() {
        super(objClass -> NoopObjectCache.getInstance());
    }

    public static StaticObjectCacheBinder getInstance() {
        return INSTANCE;
    }
}
