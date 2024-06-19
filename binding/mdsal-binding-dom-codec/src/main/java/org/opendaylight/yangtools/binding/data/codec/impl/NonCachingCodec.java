/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

class NonCachingCodec<D extends DataObject> implements BindingNormalizedNodeCachingCodec<D> {

    private final BindingNormalizedNodeCodec<D> delegate;

    protected NonCachingCodec(final BindingNormalizedNodeCodec<D> delegate) {
        this.delegate = delegate;
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> data) {
        return delegate.deserialize(data);
    }

    @Override
    public NormalizedNode<?, ?> serialize(final D data) {
        return delegate.serialize(data);
    }

    @Override
    public void close() {
        // NOOP
    }

}
