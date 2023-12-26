/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Interner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode.Builder;

/**
 * A {@link Builder} interning {@link LeafNode}s via an {@link Interner}.
 *
 * @param <T> value type
 */
public final class InterningLeafNodeBuilder<T> implements Builder<T> {
    private final Interner<LeafNode<T>> interner;
    private final Builder<T> delegate;

    public InterningLeafNodeBuilder(final Builder<T> delegate, final Interner<LeafNode<T>> interner) {
        this.delegate = requireNonNull(delegate);
        this.interner = requireNonNull(interner);
    }

    @Override
    public Builder<T> withName(final NodeIdentifier nodeIdentifier) {
        verifyNotNull(delegate.withName(nodeIdentifier));
        return this;
    }

    @Override
    public Builder<T> withName(final QName qname) {
        verifyNotNull(delegate.withName(qname));
        return this;
    }

    @Override
    public Builder<T> withBody(final T value) {
        verifyNotNull(delegate.withBody(value));
        return this;
    }

    @Override
    public LeafNode<T> build() {
        return interner.intern(delegate.build());
    }
}
