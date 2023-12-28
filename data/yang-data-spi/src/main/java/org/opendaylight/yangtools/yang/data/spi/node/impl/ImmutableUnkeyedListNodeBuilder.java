/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

public final class ImmutableUnkeyedListNodeBuilder implements UnkeyedListNode.Builder {
    private List<UnkeyedListEntryNode> value;
    private NodeIdentifier nodeIdentifier;
    private boolean dirty;

    public ImmutableUnkeyedListNodeBuilder() {
        value = new LinkedList<>();
        dirty = false;
    }

    public ImmutableUnkeyedListNodeBuilder(final int sizeHint) {
        this();
    }


    protected ImmutableUnkeyedListNodeBuilder(final ImmutableUnkeyedListNode node) {
        nodeIdentifier = node.name();
        // FIXME: clean this up, notably reuse unmodified lists
        value = new LinkedList<>();
        Iterables.addAll(value, node.body());
        dirty = true;
    }

    public static UnkeyedListNode.@NonNull Builder create(final UnkeyedListNode node) {
        if (node instanceof ImmutableUnkeyedListNode immutableNode) {
            return new ImmutableUnkeyedListNodeBuilder(immutableNode);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    private void checkDirty() {
        if (dirty) {
            value = new LinkedList<>(value);
            dirty = false;
        }
    }

    @Override
    public ImmutableUnkeyedListNodeBuilder withChild(final UnkeyedListEntryNode child) {
        checkDirty();
        value.add(child);
        return this;
    }

    @Override
    public ImmutableUnkeyedListNodeBuilder withoutChild(final PathArgument key) {
        checkDirty();
        throw new UnsupportedOperationException("Children does not have identifiers.");
    }

    @Override
    public ImmutableUnkeyedListNodeBuilder withValue(final Collection<UnkeyedListEntryNode> withValue) {
        // TODO replace or putAll ?
        for (var node : withValue) {
            withChild(node);
        }

        return this;
    }

    @Override
    public ImmutableUnkeyedListNodeBuilder withNodeIdentifier(final NodeIdentifier withNodeIdentifier) {
        nodeIdentifier = withNodeIdentifier;
        return this;
    }

    @Override
    public UnkeyedListNode build() {
        dirty = true;
        return ImmutableUnkeyedListNode.of(nodeIdentifier, value);
    }

    @Override
    public ImmutableUnkeyedListNodeBuilder addChild(final UnkeyedListEntryNode child) {
        return withChild(child);
    }

    @Override
    public ImmutableUnkeyedListNodeBuilder removeChild(final PathArgument key) {
        return withoutChild(key);
    }
}
