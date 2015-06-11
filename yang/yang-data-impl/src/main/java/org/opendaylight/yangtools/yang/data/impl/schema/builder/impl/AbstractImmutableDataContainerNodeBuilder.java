/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.CloneableChildrenMap;

abstract class AbstractImmutableDataContainerNodeBuilder<I extends YangInstanceIdentifier.PathArgument, R extends DataContainerNode<I>> implements DataContainerNodeBuilder<I, R> {
    private static final int DEFAULT_CAPACITY = 4;
    private Map<YangInstanceIdentifier.PathArgument, DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?>> value;
    private I nodeIdentifier;

    /*
     * Tracks whether the builder is dirty, e.g. whether the value map has been used
     * to construct a child. If it has, we detect this condition before any further
     * modification and create a new value map with same contents. This way we do not
     * force a map copy if the builder is not reused.
     */
    private boolean dirty;

    protected AbstractImmutableDataContainerNodeBuilder() {
        this.value = new Object2ObjectOpenHashMap<>(DEFAULT_CAPACITY);
        this.dirty = false;
    }

    protected AbstractImmutableDataContainerNodeBuilder(final int sizeHint) {
        final int size = sizeHint >= 0 ? sizeHint : DEFAULT_CAPACITY;
        this.value = new Object2ObjectOpenHashMap<>(size);
        this.dirty = false;
    }

    protected AbstractImmutableDataContainerNodeBuilder(final AbstractImmutableDataContainerNode<I> node) {
        this.nodeIdentifier = node.getIdentifier();

        /*
         * This quite awkward. What we actually want to be saying here is: give me
         * a copy-on-write view of your children. The API involved in that could be
         * a bit hairy, so we do the next best thing and rely on the fact that the
         * returned object implements a specific interface, which leaks the functionality
         * we need.
         */
        this.value = node.getChildren();
        this.dirty = true;
    }

    protected final I getNodeIdentifier() {
        return nodeIdentifier;
    }

    protected final DataContainerChild<? extends PathArgument, ?> getChild(final PathArgument child) {
        return value.get(child);
    }

    protected final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> buildValue() {
        dirty = true;
        if (value instanceof Object2ObjectOpenHashMap) {
            ((Object2ObjectOpenHashMap<?, ?>) value).trim();
        }
        return value;
    }

    private void checkDirty() {
        if (dirty) {
            if (value instanceof CloneableChildrenMap) {
                value = ((CloneableChildrenMap) value).createMutableClone();
            } else if (value instanceof Object2ObjectOpenHashMap) {
                value = ((Object2ObjectOpenHashMap<YangInstanceIdentifier.PathArgument, DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?>>) value).clone();
            } else {
                value = new Object2ObjectOpenHashMap<>(value);
            }
            dirty = false;
        }
    }

    @Override
    public DataContainerNodeBuilder<I, R> withValue(final Collection<DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?>> value) {
        // TODO Replace or putAll ?
        for (final DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> dataContainerChild : value) {
            withChild(dataContainerChild);
        }
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> withChild(final DataContainerChild<?, ?> child) {
        checkDirty();
        this.value.put(child.getIdentifier(), child);
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> withoutChild(final PathArgument key) {
        checkDirty();
        this.value.remove(key);
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> withNodeIdentifier(final I nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> addChild(
            final DataContainerChild<? extends PathArgument, ?> child) {
        return withChild(child);
    }

    @Override
    public NormalizedNodeContainerBuilder<I, PathArgument, DataContainerChild<? extends PathArgument, ?>, R> removeChild(final PathArgument key) {
        return withoutChild(key);
    }
}
