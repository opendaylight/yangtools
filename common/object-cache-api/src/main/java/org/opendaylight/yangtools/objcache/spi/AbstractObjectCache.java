/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

import org.opendaylight.yangtools.concepts.ProductAwareBuilder;
import org.opendaylight.yangtools.objcache.ObjectCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;

/**
 * Abstract object cache implementation. This implementation takes care
 * of interacting with the user and manages interaction with the Garbage
 * Collector (via soft references). Subclasses are expected to provide
 * a backing {@link Cache} instance and provide the
 */
public abstract class AbstractObjectCache implements ObjectCache {
	/**
	 * Key used when looking up a ProductAwareBuilder product. We assume
	 * the builder is not being modified for the duration of the lookup,
	 * anything else is the user's fault.
	 */
	private static final class BuilderKey {
		private final ProductAwareBuilder<?> builder;

		private BuilderKey(final ProductAwareBuilder<?> builder) {
			this.builder = Preconditions.checkNotNull(builder);
		}

		@Override
		public int hashCode() {
			return builder.productHashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			return builder.productEquals(obj);
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(AbstractObjectCache.class);
	private final Cache<Object, Object> cache;

	protected AbstractObjectCache(final Cache<Object, Object> cache) {
		this.cache = Preconditions.checkNotNull(cache);
	}

	@Override
	public final <B extends ProductAwareBuilder<P>, P> P getProduct(final B builder) {
		LOG.debug("Looking up product for {}", builder);

		@SuppressWarnings("unchecked")
		P ret = (P) cache.getIfPresent(new BuilderKey(builder));
		if (ret != null) {
			return ret;
		}

		ret = Preconditions.checkNotNull(builder.toInstance());
		cache.put(ret, ret);
		return ret;
	}

	@Override
	public final <T> T getReference(final T object) {
		LOG.debug("Looking up reference for {}", object);
		if (object == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		final T ret = (T) cache.getIfPresent(object);
		LOG.debug("Reference for {} returned {}", object, ret);
		if (ret != null) {
			return ret;
		}

		cache.put(object, object);
		return object;
	}
}
