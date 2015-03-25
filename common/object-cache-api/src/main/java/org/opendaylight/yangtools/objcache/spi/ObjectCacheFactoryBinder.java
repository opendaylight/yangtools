/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

/**
 * Interface binding an implementation into ObjectCacheFactory.
 */
public interface ObjectCacheFactoryBinder {
    /**
     * Get the implementation-specific cache factory.
     *
     * @return Implementation-specific factory.
     */
    IObjectCacheFactory getProductCacheFactory();
}
