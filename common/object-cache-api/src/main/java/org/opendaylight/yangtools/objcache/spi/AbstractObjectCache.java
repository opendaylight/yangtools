/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableSoftReference;
import com.google.common.cache.Cache;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.ProductAwareBuilder;
import org.opendaylight.yangtools.objcache.ObjectCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @VisibleForTesting
    static final class BuilderKey {
        private final ProductAwareBuilder<?> builder;

        private BuilderKey(final ProductAwareBuilder<?> builder) {
            this.builder = requireNonNull(builder);
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
            if (obj instanceof SoftKey) {
                obj = ((SoftKey<?>)obj).get();
            }

            return builder.productEquals(obj);
        }
    }

    /**
     * Key used in the underlying map. It is essentially a soft reference, with
     * slightly special properties.
     *
     * <p>
     * It acts as a proxy for the object it refers to and essentially delegates
     * to it. There are three exceptions here:
     *
     * <p>
     * 1) This key needs to have a cached hash code. The requirement is that the
     *    key needs to be able to look itself up after the reference to the object
     *    has been cleared (and thus we can no longer look it up from there). One
     *    typical container where we are stored are HashMaps -- and they need it
     *    to be constant.
     * 2) This key does not tolerate checks to see if its equal to null. While we
     *    could return false, we want to catch offenders who try to store nulls
     *    in the cache.
     * 3) This key inverts the check for equality, e.g. it calls equals() on the
     *    object which was passed to its equals(). Instead of supplying itself,
     *    it supplies the referent. If the soft reference is cleared, such check
     *    will return false, which is fine as it prevents normal lookup from
     *    seeing the cleared key. Removal is handled by the explicit identity
     *    check.
     */
    protected abstract static class SoftKey<T> extends FinalizableSoftReference<T> {
        private final int hashCode;

        public SoftKey(final T referent, final FinalizableReferenceQueue queue) {
            super(requireNonNull(referent), queue);
            hashCode = referent.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }

            // Order is important: we do not want to call equals() on ourselves!
            return this == obj || obj.equals(get());
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractObjectCache.class);
    private final FinalizableReferenceQueue queue;
    private final Cache<SoftKey<?>, Object> cache;

    protected AbstractObjectCache(final Cache<SoftKey<?>, Object> cache, final FinalizableReferenceQueue queue) {
        this.queue = requireNonNull(queue);
        this.cache = requireNonNull(cache);
    }

    protected <T> SoftKey<T> createSoftKey(final T object) {
        /*
         * This may look like a race (having a soft reference and not have
         * it in the cache). In fact this is protected by the fact we still
         * have a strong reference on the object in our arguments and that
         * reference survives past method return since we return it.
         */
        return new SoftKey<T>(object, queue) {
            @Override
            public void finalizeReferent() {
                /*
                 * NOTE: while it may be tempting to add "object" into this
                 *       trace message, do not ever do that: it would retain
                 *       a strong reference, preventing collection.
                 */
                LOG.trace("Invalidating key {}", this);
                cache.invalidate(this);
            }
        };
    }

    @Override
    public final <B extends ProductAwareBuilder<P>, P> P getProduct(@Nonnull final B builder) {
        throw new UnsupportedOperationException();
//        LOG.debug("Looking up product for {}", builder);
//
//        @SuppressWarnings("unchecked")
//        final P ret = (P) cache.getIfPresent(new BuilderKey(builder));
//        return ret == null ? put(Preconditions.checkNotNull(builder.toInstance())) : ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getReference(final T object) {
        LOG.debug("Looking up reference for {}", object);
        if (object == null) {
            return null;
        }

        final SoftKey<T> key = createSoftKey(object);
        try {
            return (T) cache.get(key, () -> object);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Failed to load value", e);
        }
    }
}
