/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class ListNodeCodecContext<D extends DataObject> extends DataObjectCodecContext<D, ListSchemaNode> {
    ListNodeCodecContext(final DataContainerCodecPrototype<ListSchemaNode> prototype) {
        super(prototype);
    }

    ListNodeCodecContext(final DataContainerCodecPrototype<ListSchemaNode> prototype,
            final Method keyMethod) {
        super(prototype, keyMethod);
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> node) {
        if (node instanceof MapEntryNode) {
            return fromMapEntry((MapEntryNode) node);
        } else if (node instanceof UnkeyedListEntryNode) {
            return fromUnkeyedListEntry((UnkeyedListEntryNode) node);
        } else {
            throw new IllegalStateException("Unsupported data type " + node.getClass());
        }
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> node) {
        if (node instanceof MapNode) {
            return fromMap((MapNode) node);
        } else if (node instanceof MapEntryNode) {
            return fromMapEntry((MapEntryNode) node);
        } else if (node instanceof UnkeyedListNode) {
            return fromUnkeyedList((UnkeyedListNode) node);
        } else if (node instanceof UnkeyedListEntryNode) {
            return fromUnkeyedListEntry((UnkeyedListEntryNode) node);
        } else {
            throw new IllegalStateException("Unsupported data type " + node.getClass());
        }
    }

    private Object fromMap(final MapNode map) {
        final Collection<MapEntryNode> value = map.getValue();
        // This should never happen, but we do need to ensure users never see an empty Map
        return value.isEmpty() ? null : fromMap(map, value);
    }

    Object fromMap(final MapNode map, final Collection<MapEntryNode> value) {
        // FIXME: Could be this lazy transformed list?
        final Builder<D> builder = ImmutableList.builderWithExpectedSize(value.size());
        for (MapEntryNode node : value) {
            builder.add(createBindingProxy(node));
        }
        return builder.build();
    }

    final @NonNull D fromMapEntry(final MapEntryNode node) {
        return createBindingProxy(node);
    }

    private @NonNull D fromUnkeyedListEntry(final UnkeyedListEntryNode node) {
        return createBindingProxy(node);
    }

    private List<D> fromUnkeyedList(final UnkeyedListNode nodes) {
        final Collection<UnkeyedListEntryNode> value = nodes.getValue();
        if (value.isEmpty()) {
            // This should never happen, but we do need to ensure users never see an empty List
            return null;
        }

        // FIXME: Could be this lazy transformed list?
        final Builder<D> builder = ImmutableList.builderWithExpectedSize(value.size());
        for (UnkeyedListEntryNode node : value) {
            builder.add(fromUnkeyedListEntry(node));
        }
        return builder.build();
    }
}
