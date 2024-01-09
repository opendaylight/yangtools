/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.NodeStep;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

sealed class ListCodecContext<D extends DataObject> extends DataObjectCodecContext<D, ListRuntimeType>
        permits MapCodecContext {
    ListCodecContext(final Class<D> cls, final ListRuntimeType list, final CodecContextFactory factory) {
        this(new ListCodecPrototype(new NodeStep<>(cls), list, factory));
    }

    ListCodecContext(final ListCodecPrototype prototype) {
        super(prototype);
    }

    ListCodecContext(final ListCodecPrototype prototype, final Method keyMethod) {
        super(prototype, keyMethod);
    }

    @Override
    public D deserialize(final NormalizedNode node) {
        final var nonnull = requireNonNull(node);
        if (nonnull instanceof MapEntryNode mapEntry) {
            return createBindingProxy(mapEntry);
        } else if (nonnull instanceof UnkeyedListEntryNode unkeyedEntry) {
            return createBindingProxy(unkeyedEntry);
        } else {
            throw new IllegalArgumentException("Expecting either a MapEntryNode or an UnkeyedListEntryNode, not "
                + node.contract().getSimpleName());
        }
    }

    @Override
    protected Object deserializeObject(final NormalizedNode node) {
        if (node instanceof MapNode map) {
            return fromMap(map);
        } else if (node instanceof MapEntryNode mapEntry) {
            return createBindingProxy(mapEntry);
        } else if (node instanceof UnkeyedListNode list) {
            return fromUnkeyedList(list);
        } else if (node instanceof UnkeyedListEntryNode listEntry) {
            return createBindingProxy(listEntry);
        } else {
            throw new IllegalStateException("Unsupported data type " + node.contract().getSimpleName());
        }
    }

    @NonNull Object fromMap(final MapNode map, final int size) {
        return LazyBindingList.of(this, size, map.body());
    }

    private Object fromMap(final MapNode map) {
        final int size;
        // This should never happen, but we do need to ensure users never see an empty Map
        return (size = map.size()) == 0 ? null : fromMap(map, size);
    }

    private List<D> fromUnkeyedList(final UnkeyedListNode node) {
        final int size;
        // This should never happen, but we do need to ensure users never see an empty List
        return (size = node.size()) == 0 ? null : LazyBindingList.of(this, size, node.body());
    }
}
