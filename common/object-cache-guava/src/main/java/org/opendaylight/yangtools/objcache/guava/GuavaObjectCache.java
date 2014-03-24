/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.guava;

import org.opendaylight.yangtools.objcache.spi.AbstractObjectCache;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;

final class GuavaObjectCache extends AbstractObjectCache {
	private static final int DEFAULT_MAX_DRAIN_ITEMS = 32;
	private final Cache<Object, Object> currentCache;
	private final int maxDrainItems;

	public GuavaObjectCache(final CacheBuilderSpec spec) {
		this(spec, DEFAULT_MAX_DRAIN_ITEMS);
	}

	public GuavaObjectCache(final CacheBuilderSpec spec, final int maxDrainItems) {
		Preconditions.checkArgument(maxDrainItems > 0, "maxDrainItems must be positive");
		currentCache = CacheBuilder.from(spec).softValues().build();
		this.maxDrainItems = maxDrainItems;
	}

	@Override
	protected Cache<Object, Object> cache() {
		return currentCache;
	}

	@Override
	protected void invalidateKeys(final Cache<Object, Object> cache, final Supplier<Object> keys) {
		for (int i = 0; i < maxDrainItems; ++i) {
			final Object key = keys.get();
			if (key != null) {
				cache.invalidate(key);
			}
		}
	}
}
