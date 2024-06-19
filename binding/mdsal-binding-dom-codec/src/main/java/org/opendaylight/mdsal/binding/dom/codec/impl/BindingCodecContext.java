/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.kohsuke.MetaInfServices;
import org.opendaylight.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.dom.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.spi.AbstractBindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingSchemaMapping;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.concepts.IllegalArgumentCodec;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.ValueNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@MetaInfServices(value = BindingDOMCodecServices.class)
@Singleton
public final class BindingCodecContext extends AbstractBindingNormalizedNodeSerializer
        implements BindingDOMCodecServices, Immutable, CodecContextFactory, DataObjectSerializerRegistry {
    private final class DataObjectSerializerProxy implements DataObjectSerializer, Delegator<DataObjectStreamer<?>> {
        private final @NonNull DataObjectStreamer<?> delegate;

        DataObjectSerializerProxy(final DataObjectStreamer<?> delegate) {
            this.delegate = requireNonNull(delegate);
        }

        @Override
        public DataObjectStreamer<?> getDelegate() {
            return delegate;
        }

        @Override
        public void serialize(final DataObject obj, final BindingStreamEventWriter stream) throws IOException {
            delegate.serialize(BindingCodecContext.this, obj, stream);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(BindingCodecContext.class);

    private final LoadingCache<Class<?>, DataObjectStreamer<?>> streamers = CacheBuilder.newBuilder().build(
        new CacheLoader<Class<?>, DataObjectStreamer<?>>() {
            @Override
            public DataObjectStreamer<?> load(final Class<?> key) throws ReflectiveOperationException {
                final Class<?> streamer = DataObjectStreamerGenerator.generateStreamer(loader, BindingCodecContext.this,
                    key);
                final Field instance = streamer.getDeclaredField(DataObjectStreamerGenerator.INSTANCE_FIELD);
                return (DataObjectStreamer<?>) instance.get(null);
            }
        });
    private final LoadingCache<Class<?>, DataObjectSerializer> serializers = CacheBuilder.newBuilder().build(
        new CacheLoader<Class<?>, DataObjectSerializer>() {
            @Override
            public DataObjectSerializer load(final Class<?> key) throws ExecutionException {
                return new DataObjectSerializerProxy(streamers.get(key));
            }
        });

    private final @NonNull CodecClassLoader loader = CodecClassLoader.create();
    private final @NonNull InstanceIdentifierCodec instanceIdentifierCodec;
    private final @NonNull IdentityCodec identityCodec;
    private final @NonNull BindingRuntimeContext context;
    private final SchemaRootCodecContext<?> root;

    public BindingCodecContext() {
        this(ServiceLoader.load(BindingRuntimeContext.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to load BindingRuntimeContext")));
    }

    @Inject
    public BindingCodecContext(final BindingRuntimeContext context) {
        this.context = requireNonNull(context, "Binding Runtime Context is required.");
        this.root = SchemaRootCodecContext.create(this);
        this.identityCodec = new IdentityCodec(context);
        this.instanceIdentifierCodec = new InstanceIdentifierCodec(this);
    }

    @Override
    public BindingRuntimeContext getRuntimeContext() {
        return context;
    }

    @Override
    public CodecClassLoader getLoader() {
        return loader;
    }

    @Override
    public IdentityCodec getIdentityCodec() {
        return identityCodec;
    }

    @Override
    public BindingInstanceIdentifierCodec getInstanceIdentifierCodec() {
        return instanceIdentifierCodec;
    }

    @Override
    public DataObjectSerializer getEventStreamSerializer(final Class<?> type) {
        return serializers.getUnchecked(type);
    }

    @Override
    public DataObjectStreamer<?> getDataObjectSerializer(final Class<?> type) {
        return streamers.getUnchecked(type);
    }

    @Override
    public DataObjectSerializer getSerializer(final Class<? extends DataObject> type) {
        return serializers.getUnchecked(type);
    }

    @Override
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriterAndIdentifier(
            final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        final List<YangInstanceIdentifier.PathArgument> yangArgs = new LinkedList<>();
        final DataContainerCodecContext<?,?> codecContext = getCodecContextNode(path, yangArgs);
        return new SimpleEntry<>(YangInstanceIdentifier.create(yangArgs), codecContext.createWriter(domWriter));
    }

    @Override
    public BindingStreamEventWriter newWriter(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return getCodecContextNode(path, null).createWriter(domWriter);
    }

    @Override
    public BindingStreamEventWriter newRpcWriter(final Class<? extends DataContainer> rpcInputOrOutput,
            final NormalizedNodeStreamWriter domWriter) {
        return root.getRpc(rpcInputOrOutput).createWriter(domWriter);
    }

    @Override
    public BindingStreamEventWriter newNotificationWriter(final Class<? extends Notification> notification,
            final NormalizedNodeStreamWriter domWriter) {
        return root.getNotification(notification).createWriter(domWriter);
    }

    @Override
    public BindingStreamEventWriter newActionInputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return getActionCodec(action).input().createWriter(domWriter);
    }

    @Override
    public BindingStreamEventWriter newActionOutputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return getActionCodec(action).output().createWriter(domWriter);
    }

    DataContainerCodecContext<?,?> getCodecContextNode(final InstanceIdentifier<?> binding,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        DataContainerCodecContext<?,?> currentNode = root;
        for (final InstanceIdentifier.PathArgument bindingArg : binding.getPathArguments()) {
            currentNode = currentNode.bindingPathArgumentChild(bindingArg, builder);
            checkArgument(currentNode != null, "Supplied Instance Identifier %s is not valid.", binding);
        }
        return currentNode;
    }

    /**
     * Multi-purpose utility function. Traverse the codec tree, looking for
     * the appropriate codec for the specified {@link YangInstanceIdentifier}.
     * As a side-effect, gather all traversed binding {@link InstanceIdentifier.PathArgument}s
     * into the supplied collection.
     *
     * @param dom {@link YangInstanceIdentifier} which is to be translated
     * @param bindingArguments Collection for traversed path arguments
     * @return Codec for target node, or @null if the node does not have a
     *         binding representation (choice, case, leaf).
     *
     */
    @Nullable BindingDataObjectCodecTreeNode<?> getCodecContextNode(final @NonNull YangInstanceIdentifier dom,
            final @Nullable Collection<InstanceIdentifier.PathArgument> bindingArguments) {
        NodeCodecContext currentNode = root;
        ListNodeCodecContext<?> currentList = null;

        for (final YangInstanceIdentifier.PathArgument domArg : dom.getPathArguments()) {
            checkArgument(currentNode instanceof DataContainerCodecContext,
                "Unexpected child of non-container node %s", currentNode);
            final DataContainerCodecContext<?,?> previous = (DataContainerCodecContext<?, ?>) currentNode;
            final NodeCodecContext nextNode = previous.yangPathArgumentChild(domArg);

            /*
             * List representation in YANG Instance Identifier consists of two
             * arguments: first is list as a whole, second is list as an item so
             * if it is /list it means list as whole, if it is /list/list - it
             * is wildcarded and if it is /list/list[key] it is concrete item,
             * all this variations are expressed in Binding Aware Instance
             * Identifier as Item or IdentifiableItem
             */
            if (currentList != null) {
                checkArgument(currentList == nextNode,
                        "List should be referenced two times in YANG Instance Identifier %s", dom);

                // We entered list, so now we have all information to emit
                // list path using second list argument.
                if (bindingArguments != null) {
                    bindingArguments.add(currentList.getBindingPathArgument(domArg));
                }
                currentList = null;
                currentNode = nextNode;
            } else if (nextNode instanceof ListNodeCodecContext) {
                // We enter list, we do not update current Node yet,
                // since we need to verify
                currentList = (ListNodeCodecContext<?>) nextNode;
            } else if (nextNode instanceof ChoiceNodeCodecContext) {
                // We do not add path argument for choice, since
                // it is not supported by binding instance identifier.
                currentNode = nextNode;
            } else if (nextNode instanceof DataContainerCodecContext) {
                if (bindingArguments != null) {
                    bindingArguments.add(((DataContainerCodecContext<?, ?>) nextNode).getBindingPathArgument(domArg));
                }
                currentNode = nextNode;
            } else if (nextNode instanceof ValueNodeCodecContext) {
                LOG.debug("Instance identifier referencing a leaf is not representable ({})", dom);
                return null;
            }
        }

        // Algorithm ended in list as whole representation
        // we sill need to emit identifier for list
        if (currentNode instanceof ChoiceNodeCodecContext) {
            LOG.debug("Instance identifier targeting a choice is not representable ({})", dom);
            return null;
        }
        if (currentNode instanceof CaseNodeCodecContext) {
            LOG.debug("Instance identifier targeting a case is not representable ({})", dom);
            return null;
        }

        if (currentList != null) {
            if (bindingArguments != null) {
                bindingArguments.add(currentList.getBindingPathArgument(null));
            }
            return currentList;
        }
        if (currentNode != null) {
            verify(currentNode instanceof BindingDataObjectCodecTreeNode, "Illegal return node %s for identifier %s",
                currentNode, dom);
            return (BindingDataObjectCodecTreeNode<?>) currentNode;
        }
        return null;
    }

    NotificationCodecContext<?> getNotificationContext(final SchemaPath notification) {
        return root.getNotification(notification);
    }

    RpcInputCodec<?> getRpcInputCodec(final SchemaPath path) {
        return root.getRpc(path);
    }

    ActionCodecContext getActionCodec(final Class<? extends Action<?, ?, ?>> action) {
        return root.getAction(action);
    }

    @Override
    public ImmutableMap<Method, ValueNodeCodecContext> getLeafNodes(final Class<?> parentClass,
            final DataNodeContainer childSchema) {
        final Map<String, DataSchemaNode> getterToLeafSchema = new HashMap<>();
        for (final DataSchemaNode leaf : childSchema.getChildNodes()) {
            if (leaf instanceof TypedDataSchemaNode || leaf instanceof AnyxmlSchemaNode
                    || leaf instanceof AnydataSchemaNode) {
                getterToLeafSchema.put(BindingSchemaMapping.getGetterMethodName(leaf), leaf);
            }
        }
        return getLeafNodesUsingReflection(parentClass, getterToLeafSchema);
    }

    private ImmutableMap<Method, ValueNodeCodecContext> getLeafNodesUsingReflection(
            final Class<?> parentClass, final Map<String, DataSchemaNode> getterToLeafSchema) {
        final Map<Method, ValueNodeCodecContext> leaves = new HashMap<>();
        for (final Method method : parentClass.getMethods()) {
            if (method.getParameterCount() == 0) {
                final DataSchemaNode schema = getterToLeafSchema.get(method.getName());

                final ValueNodeCodecContext valueNode;
                if (schema instanceof LeafSchemaNode) {
                    final LeafSchemaNode leafSchema = (LeafSchemaNode) schema;

                    final Class<?> valueType = method.getReturnType();
                    final IllegalArgumentCodec<Object, Object> codec = getCodec(valueType, leafSchema.getType());
                    valueNode = LeafNodeCodecContext.of(leafSchema, codec, method.getName(), valueType,
                        context.getSchemaContext());
                } else if (schema instanceof LeafListSchemaNode) {
                    final Optional<Type> optType = ClassLoaderUtils.getFirstGenericParameter(
                        method.getGenericReturnType());
                    checkState(optType.isPresent(), "Failed to find return type for %s", method);

                    final Class<?> valueType;
                    final Type genericType = optType.get();
                    if (genericType instanceof Class<?>) {
                        valueType = (Class<?>) genericType;
                    } else if (genericType instanceof ParameterizedType) {
                        valueType = (Class<?>) ((ParameterizedType) genericType).getRawType();
                    } else {
                        throw new IllegalStateException("Unexpected return type " + genericType);
                    }

                    final LeafListSchemaNode leafListSchema = (LeafListSchemaNode) schema;
                    final IllegalArgumentCodec<Object, Object> codec = getCodec(valueType, leafListSchema.getType());
                    valueNode = new LeafSetNodeCodecContext(leafListSchema, codec, method.getName());
                } else if (schema instanceof AnyxmlSchemaNode) {
                    valueNode = new OpaqueNodeCodecContext.Anyxml<>((AnyxmlSchemaNode) schema, method.getName(),
                            opaqueReturnType(method), loader);
                } else if (schema instanceof AnydataSchemaNode) {
                    valueNode = new OpaqueNodeCodecContext.Anydata<>((AnydataSchemaNode) schema, method.getName(),
                            opaqueReturnType(method), loader);
                } else {
                    verify(schema == null, "Unhandled schema %s for method %s", schema, method);
                    // We do not have schema for leaf, so we will ignore it (e.g. getClass).
                    continue;
                }

                leaves.put(method, valueNode);
            }
        }
        return ImmutableMap.copyOf(leaves);
    }

    // FIXME: this is probably not right w.r.t. nulls
    IllegalArgumentCodec<Object, Object> getCodec(final Class<?> valueType, final TypeDefinition<?> instantiatedType) {
        if (Class.class.equals(valueType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final IllegalArgumentCodec<Object, Object> casted = (IllegalArgumentCodec) identityCodec;
            return casted;
        } else if (InstanceIdentifier.class.equals(valueType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final IllegalArgumentCodec<Object, Object> casted = (IllegalArgumentCodec) instanceIdentifierCodec;
            return casted;
        } else if (BindingReflections.isBindingClass(valueType)) {
            return getCodecForBindingClass(valueType, instantiatedType);
        }
        return ValueTypeCodec.NOOP_CODEC;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    // FIXME: this is probably not right w.r.t. nulls
    private IllegalArgumentCodec<Object, Object> getCodecForBindingClass(final Class<?> valueType,
            final TypeDefinition<?> typeDef) {
        if (typeDef instanceof IdentityrefTypeDefinition) {
            return ValueTypeCodec.encapsulatedValueCodecFor(valueType, typeDef, identityCodec);
        } else if (typeDef instanceof InstanceIdentifierTypeDefinition) {
            return ValueTypeCodec.encapsulatedValueCodecFor(valueType, typeDef, instanceIdentifierCodec);
        } else if (typeDef instanceof UnionTypeDefinition) {
            final Callable<UnionTypeCodec> unionLoader = UnionTypeCodec.loader(valueType, (UnionTypeDefinition) typeDef,
                this);
            try {
                return unionLoader.call();
            } catch (final Exception e) {
                throw new IllegalStateException("Unable to load codec for " + valueType, e);
            }
        } else if (typeDef instanceof LeafrefTypeDefinition) {
            final Entry<GeneratedType, WithStatus> typeWithSchema = context.getTypeWithSchema(valueType);
            final WithStatus schema = typeWithSchema.getValue();
            checkState(schema instanceof TypeDefinition);
            return getCodec(valueType, (TypeDefinition<?>) schema);
        }
        return ValueTypeCodec.getCodecFor(valueType, typeDef);
    }

    @Override
    public IdentifiableItemCodec getPathArgumentCodec(final Class<?> listClz, final ListSchemaNode schema) {
        final Optional<Class<Identifier<?>>> optIdentifier = ClassLoaderUtils.findFirstGenericArgument(listClz,
                Identifiable.class);
        checkState(optIdentifier.isPresent(), "Failed to find identifier for %s", listClz);

        final Class<Identifier<?>> identifier = optIdentifier.get();
        final Map<QName, ValueContext> valueCtx = new HashMap<>();
        for (final ValueNodeCodecContext leaf : getLeafNodes(identifier, schema).values()) {
            final QName name = leaf.getDomPathArgument().getNodeType();
            valueCtx.put(name, new ValueContext(identifier, leaf));
        }
        return IdentifiableItemCodec.of(schema, identifier, listClz, valueCtx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataObject> BindingDataObjectCodecTreeNode<T> getSubtreeCodec(final InstanceIdentifier<T> path) {
        // TODO Do we need defensive check here?
        return (BindingDataObjectCodecTreeNode<T>) getCodecContextNode(path, null);
    }

    @Override
    public BindingCodecTreeNode getSubtreeCodec(final YangInstanceIdentifier path) {
        return getCodecContextNode(path, null);
    }

    @Override
    public BindingCodecTreeNode getSubtreeCodec(final Absolute path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public YangInstanceIdentifier toYangInstanceIdentifier(final InstanceIdentifier<?> binding) {
        return instanceIdentifierCodec.fromBinding(binding);
    }

    @Override
    public <T extends DataObject> InstanceIdentifier<T> fromYangInstanceIdentifier(final YangInstanceIdentifier dom) {
        return instanceIdentifierCodec.toBinding(dom);
    }

    @Override
    public <T extends DataObject> Entry<YangInstanceIdentifier, NormalizedNode<?,?>> toNormalizedNode(
            final InstanceIdentifier<T> path, final T data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        // We create Binding Stream Writer which translates from Binding to Normalized Nodes
        final Entry<YangInstanceIdentifier, BindingStreamEventWriter> writeCtx = newWriterAndIdentifier(path,
            domWriter);

        // We get serializer which reads binding data and uses Binding To Normalized Node writer to write result
        try {
            getSerializer(path.getTargetType()).serialize(data, writeCtx.getValue());
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing path {} data {}", path, data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return new SimpleEntry<>(writeCtx.getKey(),result.getResult());
    }

    @Override
    public Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode<?, ?> data) {
        if (notBindingRepresentable(data)) {
            return null;
        }

        final List<PathArgument> builder = new ArrayList<>();
        final BindingDataObjectCodecTreeNode<?> codec = getCodecContextNode(path, builder);
        if (codec == null) {
            if (data != null) {
                LOG.warn("Path {} does not have a binding equivalent, should have been caught earlier ({})", path,
                    data.getClass());
            }
            return null;
        }

        final DataObject lazyObj = codec.deserialize(data);
        final InstanceIdentifier<?> bindingPath = InstanceIdentifier.create(builder);
        return new SimpleEntry<>(bindingPath, lazyObj);
    }

    @Override
    public Notification fromNormalizedNodeNotification(final SchemaPath path, final ContainerNode data) {
        return getNotificationContext(path).deserialize(data);
    }

    @Override
    public Notification fromNormalizedNodeNotification(final SchemaPath path, final ContainerNode data,
            final Instant eventInstant) {
        return eventInstant == null ? fromNormalizedNodeNotification(path, data)
                : getNotificationContext(path).deserialize(data, eventInstant);

    }

    @Override
    public DataObject fromNormalizedNodeRpcData(final SchemaPath path, final ContainerNode data) {
        return getRpcInputCodec(path).deserialize(data);
    }

    @Override
    public <T extends RpcInput> T fromNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode input) {
        return (T) requireNonNull(getActionCodec(action).input().deserialize(requireNonNull(input)));
    }

    @Override
    public <T extends RpcOutput> T fromNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final ContainerNode output) {
        return (T) requireNonNull(getActionCodec(action).output().deserialize(requireNonNull(output)));
    }

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    public ContainerNode toNormalizedNodeNotification(@NonNull final Notification data) {
        // FIXME: Should the cast to DataObject be necessary?
        return serializeDataObject((DataObject) data,
            (ctx, iface, domWriter) -> ctx.newNotificationWriter(iface.asSubclass(Notification.class), domWriter));
    }

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    public ContainerNode toNormalizedNodeRpcData(final DataContainer data) {
        // FIXME: Should the cast to DataObject be necessary?
        return serializeDataObject((DataObject) data, (ctx, iface, domWriter) -> ctx.newRpcWriter(iface, domWriter));
    }

    @Override
    public ContainerNode toNormalizedNodeActionInput(final Class<? extends Action<?, ?, ?>> action,
            final RpcInput input) {
        return serializeDataObject(input,(ctx, iface, domWriter) -> ctx.newActionInputWriter(action, domWriter));
    }

    @Override
    public ContainerNode toNormalizedNodeActionOutput(final Class<? extends Action<?, ?, ?>> action,
            final RpcOutput output) {
        return serializeDataObject(output, (ctx, iface, domWriter) -> ctx.newActionOutputWriter(action, domWriter));
    }

    private <T extends DataContainer> @NonNull ContainerNode serializeDataObject(final DataObject data,
            final WriterFactoryMethod<T> newWriter) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final Class<? extends DataObject> type = data.implementedInterface();
        @SuppressWarnings("unchecked")
        final BindingStreamEventWriter writer = newWriter.createWriter(this, (Class<T>) type, domWriter);
        try {
            getSerializer(type).serialize(data, writer);
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing data {}", data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return (ContainerNode) result.getResult();
    }


    private static boolean notBindingRepresentable(final NormalizedNode<?, ?> data) {
        // ValueNode covers LeafNode and LeafSetEntryNode
        return data instanceof ValueNode
                || data instanceof MapNode || data instanceof UnkeyedListNode
                || data instanceof ChoiceNode
                || data instanceof LeafSetNode;
    }

    @SuppressWarnings("rawtypes")
    private static Class<? extends OpaqueObject> opaqueReturnType(final Method method) {
        final Class<?> valueType = method.getReturnType();
        verify(OpaqueObject.class.isAssignableFrom(valueType), "Illegal value type %s", valueType);
        return valueType.asSubclass(OpaqueObject.class);
    }

    @FunctionalInterface
    private interface WriterFactoryMethod<T extends DataContainer> {
        BindingStreamEventWriter createWriter(@NonNull BindingNormalizedNodeWriterFactory factory,
                @NonNull Class<? extends T> bindingClass, @NonNull NormalizedNodeStreamWriter domWriter);
    }
}
