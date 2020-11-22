/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.ModifiableMapPhase;
import org.opendaylight.yangtools.util.UnmodifiableMapPhase;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.CloneableMap;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.LazyLeafOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractImmutableDataContainerNodeBuilder<I extends PathArgument, R extends DataContainerNode<I>>
        implements DataContainerNodeBuilder<I, R> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractImmutableDataContainerNodeBuilder.class);
    private static final int DEFAULT_CAPACITY = 4;

    // This is a run-time constant, i.e. it is set at class initialization time. We expect JIT to notice this and
    // perform DCE based on the value, so that the static newHashMap() methods end up not containing the branch at all.
    private static final boolean USE_LINKEDHASHMAP;

    static {
        USE_LINKEDHASHMAP = Boolean.getBoolean(
            "org.opendaylight.yangtools.yang.data.impl.schema.builder.retain-child-order");
        if (USE_LINKEDHASHMAP) {
            LOG.info("Initial immutable DataContainerNodes are retaining child insertion order");
        }
    }

    private Map<PathArgument, Object> value;
    private I nodeIdentifier;

    /*
     * Tracks whether the builder is dirty, e.g. whether the value map has been used
     * to construct a child. If it has, we detect this condition before any further
     * modification and create a new value map with same contents. This way we do not
     * force a map copy if the builder is not reused.
     */
    private boolean dirty;

    protected AbstractImmutableDataContainerNodeBuilder() {
        this.value = newHashMap();
        this.dirty = false;
    }

    protected AbstractImmutableDataContainerNodeBuilder(final int sizeHint) {
        if (sizeHint >= 0) {
            this.value = newHashMap(sizeHint);
        } else {
            this.value = newHashMap();
        }
        this.dirty = false;
    }

    protected AbstractImmutableDataContainerNodeBuilder(final AbstractImmutableDataContainerNode<I, R> node) {
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

    protected final @Nullable DataContainerChild getChild(final PathArgument child) {
        return LazyLeafOperations.getChild(value, child);
    }

    protected final Map<PathArgument, Object> buildValue() {
        if (value instanceof ModifiableMapPhase) {
            return ((ModifiableMapPhase<PathArgument, Object>)value).toUnmodifiableMap();
        }

        dirty = true;
        return value;
    }

    private void checkDirty() {
        if (dirty) {
            if (value instanceof UnmodifiableMapPhase) {
                value = ((UnmodifiableMapPhase<PathArgument, Object>) value).toModifiableMap();
            } else if (value instanceof CloneableMap) {
                value = ((CloneableMap<PathArgument, Object>) value).createMutableClone();
            } else {
                value = newHashMap(value);
            }
            dirty = false;
        }
    }

    @Override
    public DataContainerNodeBuilder<I, R> withValue(final Collection<DataContainerChild> withValue) {
        // TODO Replace or putAll ?
        for (final DataContainerChild dataContainerChild : withValue) {
            withChild(dataContainerChild);
        }
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> withChild(final DataContainerChild child) {
        checkDirty();
        LazyLeafOperations.putChild(value, child);
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> withoutChild(final PathArgument key) {
        checkDirty();
        this.value.remove(key);
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> withNodeIdentifier(final I withNodeIdentifier) {
        this.nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public DataContainerNodeBuilder<I, R> addChild(final DataContainerChild child) {
        return withChild(child);
    }

    @Override
    public NormalizedNodeContainerBuilder<I, PathArgument, DataContainerChild, R> removeChild(final PathArgument key) {
        return withoutChild(key);
    }

    // Static utility methods providing dispatch to proper HashMap implementation.
    private static <K, V> HashMap<K, V> newHashMap() {
        return USE_LINKEDHASHMAP ? new LinkedHashMap<>(DEFAULT_CAPACITY) : new HashMap<>(DEFAULT_CAPACITY);
    }

    private static <K, V> HashMap<K, V> newHashMap(final int size) {
        return USE_LINKEDHASHMAP ? Maps.newLinkedHashMapWithExpectedSize(size) : Maps.newHashMapWithExpectedSize(size);
    }

    private static <K, V> HashMap<K, V> newHashMap(final Map<K, V> map) {
        return USE_LINKEDHASHMAP ? new LinkedHashMap<>(map) : new HashMap<>(map);
    }
}
