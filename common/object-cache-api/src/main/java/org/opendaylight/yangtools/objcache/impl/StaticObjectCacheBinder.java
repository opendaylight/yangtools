/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.impl;

import org.opendaylight.yangtools.objcache.spi.AbstractObjectCacheBinder;

/*
 * This is a dummy placeholder implementation. The API package is bound to
 * it at compile-time, but it is not packaged and thus not present at run-time.
 */
public final class StaticObjectCacheBinder extends AbstractObjectCacheBinder {
    private StaticObjectCacheBinder() {
        super(null);
    }

    public static StaticObjectCacheBinder getInstance() {
        throw new IllegalStateException("This class should have been replaced");
    }
}
