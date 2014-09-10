/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.Map;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractImmutableDataContainerNode<K extends PathArgument> extends AbstractImmutableNormalizedNode<K, Iterable<DataContainerChild<? extends PathArgument, ?>>> implements Immutable, DataContainerNode<K> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractImmutableDataContainerNode.class);
    private final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children;

    public AbstractImmutableDataContainerNode(
            final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children, final K nodeIdentifier) {
        super(nodeIdentifier);

        /*
         * There is a code path where AbstractImmutableDataContainerNodeBuilder can reflect
         * the collection acquired via getChildren() back to us. This is typically the case
         * in the datastore where transactions cancel each other out, leaving an unmodified
         * node. In that case we want to skip wrapping the map again (and again and again).
         *
         * In a perfect world, Collection.unmodifiableMap() would be doing the instanceof
         * check which would stop the proliferation. Unfortunately this not the case and the
         * 'unmodifiable' trait is not exposed by anything we can query. Furthermore the API
         * contract there is sufficiently vague so an implementation may actually return a
         * different implementation based on input map -- for example
         * Collections.unmodifiableMap(Collections.emptyMap()) returning the same thing as
         * Collections.emptyMap().
         *
         * This means that we have to perform the instantiation here (as opposed to once at
         * class load time) and then compare the classes.
         */
        final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> pub = Collections.unmodifiableMap(children);
        if (children.getClass().equals(pub.getClass())) {
            LOG.trace("Reusing already-unmodifiable children {}", children);
            this.children = children;
        } else {
            this.children = pub;
        }
    }

    @Override
    public final Optional<DataContainerChild<? extends PathArgument, ?>> getChild(final PathArgument child) {
        return Optional.<DataContainerChild<? extends PathArgument, ?>> fromNullable(children.get(child));
    }

    @Override
    public final Iterable<DataContainerChild<? extends PathArgument, ?>> getValue() {
        return children.values();
    }

    @Override
    protected int valueHashCode() {
        return children.hashCode();
    }

    public final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> getChildren() {
        return children;
    }

    @Override
    protected boolean valueEquals(final AbstractImmutableNormalizedNode<?, ?> other) {
        if (!(other instanceof AbstractImmutableDataContainerNode<?>)) {
            return false;
        }

        return children.equals(((AbstractImmutableDataContainerNode<?>)other).children);
    }
}
