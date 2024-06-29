/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableCollection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BindingObject;
import org.opendaylight.yangtools.binding.DataContainer.Addressable;

@Beta
public interface BindingDataObjectCodecTreeNode<T extends Addressable>
        extends CommonDataObjectCodecTreeNode<T>, BindingNormalizedNodeCodec<T> {
    /**
     * Returns codec which uses caches serialization / deserialization results.
     *
     * <p>
     * Caching may introduce performance penalty to serialization / deserialization
     * but may decrease use of heap for repetitive objects.
     *
     * @param cacheSpecifier Set of objects, for which cache may be in place
     * @return Codec which uses cache for serialization / deserialization.
     */
    @NonNull BindingNormalizedNodeCachingCodec<T> createCachingCodec(
            @NonNull ImmutableCollection<Class<? extends BindingObject>> cacheSpecifier);
}
