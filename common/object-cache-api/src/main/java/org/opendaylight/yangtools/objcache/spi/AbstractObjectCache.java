/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import org.opendaylight.yangtools.concepts.ProductAwareBuilder;
import org.opendaylight.yangtools.objcache.ObjectCache;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
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
		public boolean equals(Object obj) {
			/*
			 * We can tolerate null objects coming our way, but we need
			 * to be on the lookout for WeakKeys, as we cannot pass them
			 * directly to productEquals().
			 */
			if (obj != null && obj instanceof WeakKey) {
				obj = ((WeakKey)obj).get();
			}

			return builder.productEquals(obj);
		}
	}

	/**
	 * Key used in the underlying map. It is essentially a weak reference, with
	 * slightly special properties.
	 */
	private static final class WeakKey extends WeakReference<Object> {
		private final int hashCode;

		public WeakKey(final Object referent, final ReferenceQueue<? super Object> q) {
			super(Preconditions.checkNotNull(referent), q);
			hashCode = referent.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			Preconditions.checkArgument(obj != null);
			return this == obj || obj.equals(get());
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}

	private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

	/**
	 * Get a reference to the cache. This method is invoked exactly
	 * once for each access, before cleanup is invoked.
	 *
	 * @return Cache reference.
	 */
	protected abstract Cache<Object, Object> cache();

	/**
	 * Invalidate some (or all) keys from the cache. This method is called
	 * from within user operations, so implementations need to be aware
	 * that they are running in the context of some foreign thread, possibly
	 * concurrently. The implementation is guaranteed to not see the same
	 * (identity-wise) key in concurrent invocations.
	 * 
	 * Subclasses which support dynamic replacement of backend cache, need
	 * to synchronize key invalidation and cache replacement.
	 * 
	 * @param cache Cache reference acquired at the start of operation
	 * @param keys Supplier of keys, it returns null when no more keys
	 *             need invalidation.
	 */
	protected abstract void invalidateKeys(Cache<Object, Object> cache, Supplier<Object> keys);

	private Cache<Object, Object> acquireCache() {
		final Cache<Object, Object> cache = cache();
		final Reference<?> ref = queue.poll();
		if (ref != null) {
			invalidateKeys(cache, new Supplier<Object>() {
				Object obj = ref;

				@Override
				public Object get() {
					final Object ret = obj != null ? obj : queue.poll();
					obj = null;
					return ret;
				}
			});
		}

		return cache;
	}

	private <T> T put(final Cache<Object, Object> cache, final T object) {
		cache.put(new WeakKey(object, queue), object);
		return object;
	}

	@Override
	public final <B extends ProductAwareBuilder<P>, P> P getProduct(final B builder) {
		final Cache<Object, Object> cache = acquireCache();

		@SuppressWarnings("unchecked")
		final P ret = (P) cache.getIfPresent(new BuilderKey(builder));
		return ret == null ? put(cache, Preconditions.checkNotNull(builder.toInstance())) : ret;
	}

	@Override
	public final <T> T getReference(final T object) {
		final Cache<Object, Object> cache = acquireCache();
		if (object == null) {
			return null;
		}

		@SuppressWarnings("unchecked")
		final T ret = (T) cache.getIfPresent(object);
		return ret == null ? put(cache, object) : ret;
	}
}
