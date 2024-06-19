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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCachingCodec;
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
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.IdentityEffectiveStatementImpl;

final class LeafNodeCodecContext<D extends DataObject> extends NodeCodecContext<D> implements NodeContextSupplier {

    private final YangInstanceIdentifier.PathArgument yangIdentifier;
    private final Codec<Object, Object> valueCodec;
    private final Method getter;
    private final DataSchemaNode schema;
    private final Object defaultObject;

    public LeafNodeCodecContext(final DataSchemaNode schema, final Codec<Object, Object> codec, final Method getter) {
        this.yangIdentifier = new YangInstanceIdentifier.NodeIdentifier(schema.getQName());
        this.valueCodec = Preconditions.checkNotNull(codec);
        this.getter = getter;
        this.schema = Preconditions.checkNotNull(schema);

        this.defaultObject = createDefaultObject(schema, valueCodec);
    }

    private static Object createDefaultObject(final DataSchemaNode schema, final Codec<Object, Object> codec) {
        if (schema instanceof LeafSchemaNode) {
            Object defaultValue = ((LeafSchemaNode) schema).getDefault();
            TypeDefinition<?> type = ((LeafSchemaNode) schema).getType();
            if (defaultValue != null) {
                return domValueFromString(codec, type, defaultValue);
            }
            else {
                while (type.getBaseType() != null && type.getDefaultValue() == null) {
                    type = type.getBaseType();
                }

                defaultValue = type.getDefaultValue();
                if (defaultValue != null) {
                    if (defaultValue instanceof Boolean) {
                        return codec.deserialize(defaultValue);
                    }

                    if (defaultValue instanceof IdentitySchemaNode) {
                        defaultValue = ((IdentityEffectiveStatementImpl) defaultValue).argument();
                        return codec.deserialize(defaultValue);
                    }

                    if (defaultValue instanceof ImmutableList) {
                        return codec.deserialize(ImmutableSet.copyOf((ImmutableList) defaultValue));
                    }

                    if (defaultValue instanceof List) {
                        return codec.deserialize(defaultValue);
                    }
                    return domValueFromString(codec, type, defaultValue);
                }
            }
        }
        return null;
    }

    private static Object domValueFromString(final Codec<Object, Object> codec, final TypeDefinition<?> type,
    Object defaultValue) {
        TypeDefinitionAwareCodec typeDefAwareCodec = TypeDefinitionAwareCodec.from(type);
        Object castedDefaultValue = typeDefAwareCodec.deserialize((String) defaultValue);
        return codec.deserialize(castedDefaultValue);
    }

    @Override
    protected YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return yangIdentifier;
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
        }
        if (normalizedNode instanceof LeafSetNode<?>) {
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

    @Override
    public Object getSchema() {
        return schema;
    }

    /**
     * Return the default value object.
     *
     * @return The default value object, or null if the default value is not defined.
     */
    @Nullable Object defaultObject() {
        return defaultObject;
    }
}