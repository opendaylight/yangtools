/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.objcache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.ProductAwareBuilder;

/**
 * A cache of objects. Caches are useful for reducing memory overhead
 * stemming from multiple copies of identical objects -- by putting
 * a cache in the instantiation path, one can expend some memory on
 * indexes and spend some CPU cycles on walking the index to potentially
 * end up with a reused object.
 *
 * Note that the cached objects should really be semantically {@link Immutable}.
 * This interface does not enforce that interface contract simply because
 * there are third-party objects which fulfill this contract.
 */
public interface ObjectCache {
    /**
     * Get a reference for an object which is equal to specified object.
     * The cache is free return either a cached instance, or retain the
     * object and return it back.
     *
     * @param <T> object type
     * @param object Requested object, may be null
     * @return Reference to an object which is equal to the one passed in.
     *         If @object was @null, this method returns @null.
     */
    <T> T getReference(@Nullable T object);

    /**
     * Get a reference to an object equal to the product of a builder.
     * The builder is expected to remain constant while this method
     * executes. Unlike {@link #getReference(Object)}, this method has
     * the potential of completely eliding the product instantiation.
     *
     * @param <P> produced object type
     * @param <B> builder type
     * @param builder Builder instance, may not be null
     * @return Result of builder's toInstance() product, or an equal
     *         object.
     */
    <B extends ProductAwareBuilder<P>, P> P getProduct(@Nonnull B builder);
}
