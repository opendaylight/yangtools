/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.guava;

import org.opendaylight.yangtools.objcache.spi.AbstractObjectCache;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;

final class GuavaObjectCache extends AbstractObjectCache {
    public GuavaObjectCache(final FinalizableReferenceQueue  queue) {
        super(CacheBuilder.newBuilder().softValues().build(), queue);
    }

    public GuavaObjectCache(final FinalizableReferenceQueue  queue, final CacheBuilderSpec spec) {
        super(CacheBuilder.from(spec).build(), queue);
    }
}
