/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Singleton codec for translating RPCs with implicit input statements, which are not mapped by binding spec v1. Since
 * there is no equivalent, we always return null.
 *
 * @author Robert Varga
 *
 * @param <D> Data object type
 */
final class UnmappedRpcInputCodec<D extends DataObject> implements RpcInputCodec<D> {
    private static final UnmappedRpcInputCodec<?> INSTANCE = new UnmappedRpcInputCodec<>();

    private UnmappedRpcInputCodec() {

    }

    @SuppressWarnings("unchecked")
    static <D extends DataObject> UnmappedRpcInputCodec<D> getInstance() {
        return (UnmappedRpcInputCodec<D>) INSTANCE;
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> data) {
        return null;
    }

    @Override
    public NormalizedNode<?, ?> serialize(final D data) {
        throw new UnsupportedOperationException("Serialization of " + data + " not supported");
    }
}
