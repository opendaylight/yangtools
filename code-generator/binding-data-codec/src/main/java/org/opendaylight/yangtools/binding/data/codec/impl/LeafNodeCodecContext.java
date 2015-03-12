/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class LeafNodeCodecContext<D extends DataObject> extends NodeCodecContext<D> implements NodeContextSupplier {

    private final YangInstanceIdentifier.PathArgument yangIdentifier;
    private final Codec<Object, Object> valueCodec;
    private final Method getter;

    public LeafNodeCodecContext(final DataSchemaNode schema, final Codec<Object, Object> codec, final Method getter) {
        this.yangIdentifier = new YangInstanceIdentifier.NodeIdentifier(schema.getQName());
        this.valueCodec = codec;
        this.getter = getter;
    }

    @Override
    protected YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return (yangIdentifier);
    }

    protected Codec<Object, Object> getValueCodec() {
        return valueCodec;
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException("Leaf can not be deserialized to DataObject");
    }

    @Override
    public NodeCodecContext<?> get() {
        return this;
    }

    final Method getGetter() {
        return getter;
    }

    @Override
    public BindingCodecTreeNode<?> bindingPathArgumentChild(
            PathArgument arg,
            List<org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument> builder) {
        return null;
    }

    @Override
    public BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            ImmutableCollection<Class<? extends DataObject>> cacheSpecifier) {
        return null;
    }

    @Override
    public Class<D> getBindingClass() {
        return null;
    }



    @Override
    public NormalizedNode<?, ?> serialize(D data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void writeAsNormalizedNode(D data, NormalizedNodeStreamWriter writer) {
        // TODO Auto-generated method stub

    }

    @Override
    public <E extends DataObject> BindingCodecTreeNode<E> streamChild(Class<E> childClass) {
        return null;
    }

    @Override
    public <E extends DataObject> Optional<? extends BindingCodecTreeNode<E>> possibleStreamChild(
            Class<E> childClass) {
        return null;
    }

    @Override
    public BindingCodecTreeNode<?> yangPathArgumentChild(
            org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument child) {
        return null;
    }

    @Override
    protected Object deserializeObject(NormalizedNode<?, ?> normalizedNode) {
        if (normalizedNode instanceof LeafNode<?>) {
            return valueCodec.deserialize(normalizedNode.getValue());
        } else if(normalizedNode instanceof LeafSetNode<?>) {
            @SuppressWarnings("unchecked")
            Collection<LeafSetEntryNode<Object>> domValues = ((LeafSetNode<Object>) normalizedNode).getValue();
            List<Object> result = new ArrayList<>(domValues.size());
            for (LeafSetEntryNode<Object> valueNode : domValues) {
                result.add(valueCodec.deserialize(valueNode.getValue()));
            }
            return result;
        }
        return null;
    }

}