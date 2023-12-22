/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Interner;
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode.Builder;

/**
 * Utility class for sharing instances of {@link LeafSetEntryNode}s which have low cardinality -- e.g. those which hold
 * boolean or enumeration values. Instances containing attributes are not interned.
 *
 * <p>
 * Such objects have cardinality which is capped at the product of QNAMES * TYPE_CARDINALITY, where QNAMES is the total
 * number of different QNames where the type is used and TYPE_CARDINALITY is the number of possible values for the type.
 * Boolean has cardinality of 2, enumerations have cardinality equal to the number of enum statements.
 *
 * <p>
 * The theory here is that we tend to have a large number (100K+) of entries in a few places, which could end up hogging
 * the heap retained via the DataTree with duplicate objects (same QName, same value, different object). Using this
 * utility, such objects will end up reusing the same object, preventing this overhead.
 */
public final class InterningLeafSetNodeBuilder<T> implements Builder<T> {
    private final Interner<LeafSetEntryNode<T>> interner;
    private final Builder<T> delegate;

    public InterningLeafSetNodeBuilder(final Builder<T> delegate, final Interner<LeafSetEntryNode<T>> interner) {
        this.delegate = requireNonNull(delegate);
        this.interner = requireNonNull(interner);
    }

    @Override
    public Builder<T> withNodeIdentifier(final NodeIdentifier nodeIdentifier) {
        delegate.withNodeIdentifier(nodeIdentifier);
        return this;
    }

    @Override
    public Builder<T> withValue(final Collection<LeafSetEntryNode<T>> value) {
        // FIXME: pass through interner
        delegate.withValue(value);
        return this;
    }

    @Override
    public Builder<T> withChild(final LeafSetEntryNode<T> child) {
        delegate.withChild(interner.intern(child));
        return this;
    }

    @Override
    public Builder<T> withoutChild(final PathArgument key) {
        delegate.withoutChild(key);
        return this;
    }

    @Override
    public Builder<T> withChildValue(final T child) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public Builder<T> addChild(final LeafSetEntryNode<T> child) {
        return withChild(child);
    }

    @Override
    public Builder<T> removeChild(final PathArgument key) {
        return withoutChild(key);
    }

    @Override
    public SystemLeafSetNode<T> build() {
        return delegate.build();
    }
}
