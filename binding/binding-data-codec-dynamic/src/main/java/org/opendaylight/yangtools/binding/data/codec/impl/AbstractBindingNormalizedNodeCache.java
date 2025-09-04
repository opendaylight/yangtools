/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BindingObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * An abstract cache mapping BindingObject instances to their NormalizedNode counterparts. Note that this mapping is
 * not one-to-one, as NormalizedNodes work on instantiated trees while BindingObjects are generally reused across
 * instantiations.
 *
 * @param <T> BindingObject subtype
 * @param <C> Root codec context type
 */
abstract class AbstractBindingNormalizedNodeCache<T extends BindingObject, C extends CodecContext>
        extends CacheLoader<T, NormalizedNode> {
    private final LoadingCache<T, NormalizedNode> cache;
    private final @NonNull C rootContext;

    AbstractBindingNormalizedNodeCache(final C rootContext) {
        this.rootContext = requireNonNull(rootContext);
        cache = CacheBuilder.newBuilder().weakValues().build(this);
    }

    /**
     * Returns the root codec context associated with this cache.
     *
     * @return Root codec context
     */
    final @NonNull C rootContext() {
        return rootContext;
    }

    /**
     * Returns cached NormalizedNode representation of DataObject. If the representation is not cached, serializes
     * DataObject and updates cache with representation.
     *
     * @param obj Binding object to be deserialized
     * @return NormalizedNode representation of binding object.
     */
    final NormalizedNode get(final @NonNull T obj) {
        return cache.getUnchecked(obj);
    }
}
