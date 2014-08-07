/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.Iterables;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class ListNodeCodecContext extends DataObjectCodecContext<ListSchemaNode> {

    private final Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> codec;
    private final Method keyGetter;

    ListNodeCodecContext(final DataContainerCodecPrototype<ListSchemaNode> prototype) {
        super(prototype);
        if (Identifiable.class.isAssignableFrom(bindingClass())) {
            this.codec = factory().getPathArgumentCodec(bindingClass(),schema());
            try {
                this.keyGetter = bindingClass().getMethod("getKey");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Required method not available");
            }
        } else {
            this.codec = null;
            this.keyGetter = null;
        }
    }

    @Override
    public void addYangPathArgument(final InstanceIdentifier.PathArgument arg, final List<YangInstanceIdentifier.PathArgument> builder) {

        /*
         * DOM Instance Identifier for list is always represent by two
         * entries one for map and one for children. This is also true for
         * wildcarded instance identifiers
         */
        if (builder == null) {
            return;
        }
        super.addYangPathArgument(arg, builder);
        if (arg instanceof IdentifiableItem<?, ?>) {
            builder.add(codec.serialize((IdentifiableItem<?, ?>) arg));
        } else {
            // Adding wildcarded
            super.addYangPathArgument(arg, builder);
        }
    }

    @Override
    public InstanceIdentifier.PathArgument getBindingPathArgument(
            final org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument domArg) {
        if (domArg instanceof NodeIdentifierWithPredicates) {
            return codec.deserialize((NodeIdentifierWithPredicates) domArg);
        }
        return super.getBindingPathArgument(domArg);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public NodeIdentifierWithPredicates serialize(final Identifier<?> key) {
        return codec.serialize(new IdentifiableItem(bindingClass(), key));
    }

    @Override
    protected Object dataFromNormalizedNode(final NormalizedNode<?, ?> node) {
        if (node instanceof MapNode) {
            return fromMap((MapNode) node);
        } else if(node instanceof MapEntryNode)  {
            return fromMapEntry((MapEntryNode) node);
        } else if (node instanceof UnkeyedListNode) {
            return fromUnkeyedList((UnkeyedListNode) node);
        }  else if(node instanceof UnkeyedListEntryNode) {
            return fromUnkeyedListEntry((UnkeyedListEntryNode) node);
        }
        throw new IllegalStateException("Unsupported data type " + node.getClass());
    }

    private List<DataObject> fromMap(final MapNode nodes) {
        List<DataObject> ret = new ArrayList<>(Iterables.size(nodes.getValue()));
        for(MapEntryNode node : nodes.getValue()) {
            ret.add(fromMapEntry(node));
        }
        return ret;
    }

    private DataObject fromMapEntry(final MapEntryNode node) {
        return LazyDataObject.create(this, node);
    }

    private DataObject fromUnkeyedListEntry(final UnkeyedListEntryNode node) {
        return LazyDataObject.create(this, node);
    }

    private List<DataObject> fromUnkeyedList(final UnkeyedListNode nodes) {
        // FIXME: Could be this lazy transformed list?
        List<DataObject> ret = new ArrayList<>(Iterables.size(nodes.getValue()));
        for (UnkeyedListEntryNode node : nodes.getValue()) {
            ret.add(fromUnkeyedListEntry(node));
        }
        return ret;
    }

    @Override
    Object getBindingChildValue(final Method method, final NormalizedNodeContainer dom) {
        if (method.equals(keyGetter) && dom instanceof MapEntryNode) {
            NodeIdentifierWithPredicates identifier = ((MapEntryNode) dom).getIdentifier();
            return codec.deserialize(identifier).getKey();
        }
        return super.getBindingChildValue(method, dom);
    }
}