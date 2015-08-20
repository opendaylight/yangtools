/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
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
    public BindingCodecTreeNode<?> bindingPathArgumentChild(final PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        throw new IllegalArgumentException("Leaf does not have children");
    }

    @Override
    public BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            final ImmutableCollection<Class<? extends DataObject>> cacheSpecifier) {
        throw new UnsupportedOperationException("Leaves does not support caching codec.");
    }

    @Override
    public Class<D> getBindingClass() {
        throw new UnsupportedOperationException("Leaf does not have DataObject representation");
    }

    @Override
    public NormalizedNode<?, ?> serialize(final D data) {
        throw new UnsupportedOperationException("Separete serialization of leaf node is not supported.");
    }

    @Override
    public void writeAsNormalizedNode(final D data, final NormalizedNodeStreamWriter writer) {
        throw new UnsupportedOperationException("Separete serialization of leaf node is not supported.");
    }

    @Override
    public <E extends DataObject> BindingCodecTreeNode<E> streamChild(final Class<E> childClass) {
        throw new IllegalArgumentException("Leaf does not have children");
    }

    @Override
    public <E extends DataObject> Optional<? extends BindingCodecTreeNode<E>> possibleStreamChild(
            final Class<E> childClass) {
        throw new IllegalArgumentException("Leaf does not have children");
    }

    @Override
    public BindingCodecTreeNode<?> yangPathArgumentChild(final YangInstanceIdentifier.PathArgument child) {
        throw new IllegalArgumentException("Leaf does not have children");
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        if (normalizedNode instanceof LeafNode<?>) {
            return valueCodec.deserialize(normalizedNode.getValue());
        } else if(normalizedNode instanceof LeafSetNode<?>) {
            @SuppressWarnings("unchecked")
            final Collection<LeafSetEntryNode<Object>> domValues = ((LeafSetNode<Object>) normalizedNode).getValue();
            final List<Object> result = new ArrayList<>(domValues.size());
            for (final LeafSetEntryNode<Object> valueNode : domValues) {
                result.add(valueCodec.deserialize(valueNode.getValue()));
            }
            return result;
        }
        return null;
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(getDomPathArgument().equals(arg));
        return null;
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        return getDomPathArgument();
    }

}