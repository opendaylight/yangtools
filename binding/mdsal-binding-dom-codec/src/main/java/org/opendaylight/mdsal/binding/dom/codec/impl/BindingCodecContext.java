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

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.kohsuke.MetaInfServices;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.dom.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.spi.AbstractBindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingSchemaMapping;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BaseNotification;
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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.ValueNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionAware;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(value = BindingDOMCodecServices.class)
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
    private static final @NonNull NodeIdentifier FAKE_NODEID = new NodeIdentifier(QName.create("fake", "fake"));
    private static final File BYTECODE_DIRECTORY;

    static {
        final String dir = System.getProperty("org.opendaylight.mdsal.binding.dom.codec.loader.bytecodeDumpDirectory");
        BYTECODE_DIRECTORY = Strings.isNullOrEmpty(dir) ? null : new File(dir);
    }

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

    private final @NonNull BindingClassLoader loader =
        BindingClassLoader.create(BindingCodecContext.class, BYTECODE_DIRECTORY);
    private final @NonNull InstanceIdentifierCodec instanceIdentifierCodec;
    private final @NonNull IdentityCodec identityCodec;
    private final @NonNull BindingRuntimeContext context;
    private final @NonNull RootCodecContext<?> root;

    public BindingCodecContext() {
        this(ServiceLoader.load(BindingRuntimeContext.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to load BindingRuntimeContext")));
    }

    public BindingCodecContext(final BindingRuntimeContext context) {
        this.context = requireNonNull(context, "Binding Runtime Context is required.");
        root = new RootCodecContext<>(this);
        identityCodec = new IdentityCodec(context);
        instanceIdentifierCodec = new InstanceIdentifierCodec(this);
    }

    @Override
    public BindingRuntimeContext getRuntimeContext() {
        return context;
    }

    @Override
    public BindingClassLoader getLoader() {
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
        final var yangArgs = new ArrayList<YangInstanceIdentifier.PathArgument>();
        final var codecContext = getCodecContextNode(path, yangArgs);
        return Map.entry(YangInstanceIdentifier.of(yangArgs),
            new BindingToNormalizedStreamWriter(codecContext, domWriter));
    }

    @Override
    public BindingStreamEventWriter newWriter(final InstanceIdentifier<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getCodecContextNode(path, null), domWriter);
    }

    @Override
    public BindingStreamEventWriter newRpcWriter(final Class<? extends DataContainer> rpcInputOrOutput,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(root.getRpc(rpcInputOrOutput), domWriter);
    }

    @Override
    public BindingStreamEventWriter newNotificationWriter(final Class<? extends Notification<?>> notification,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(root.getNotification(notification), domWriter);
    }

    @Override
    public BindingStreamEventWriter newActionInputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getActionCodec(action).input(), domWriter);
    }

    @Override
    public BindingStreamEventWriter newActionOutputWriter(final Class<? extends Action<?, ?, ?>> action,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getActionCodec(action).output(), domWriter);
    }

    @NonNull DataContainerCodecContext<?,?> getCodecContextNode(final InstanceIdentifier<?> binding,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        DataContainerCodecContext<?, ?> current = root;
        for (var bindingArg : binding.getPathArguments()) {
            final var next = current.bindingPathArgumentChild(bindingArg, builder);
            if (next == null) {
                throw new IllegalArgumentException("%s is not valid: parent %s does not have a child %s".formatted(
                    binding, current.bindingArg(), bindingArg));
            }
            current = next;
        }
        return current;
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
        CodecContext currentNode = root;
        ListNodeCodecContext<?> currentList = null;

        for (var domArg : dom.getPathArguments()) {
            checkArgument(currentNode instanceof DataContainerCodecContext,
                "Unexpected child of non-container node %s", currentNode);
            final var previous = (DataContainerCodecContext<?, ?>) currentNode;
            var nextNode = previous.yangPathArgumentChild(domArg);

            /**
             * Compatibility case: if it's determined the node belongs to augmentation
             * then insert augmentation path argument in between.
             */
            if (nextNode instanceof AugmentationNodeContext<?> augmContext) {
                if (bindingArguments != null) {
                    bindingArguments.add(augmContext.bindingArg());
                }
                currentNode = nextNode;
                nextNode = augmContext.yangPathArgumentChild(domArg);
            }

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
            } else if (nextNode instanceof ListNodeCodecContext<?> listNode) {
                // We enter list, we do not update current Node yet,
                // since we need to verify
                currentList = listNode;
            } else if (nextNode instanceof ChoiceNodeCodecContext) {
                // We do not add path argument for choice, since
                // it is not supported by binding instance identifier.
                currentNode = nextNode;
            } else if (nextNode instanceof DataContainerCodecContext<?, ?> containerNode) {
                if (bindingArguments != null) {
                    bindingArguments.add(containerNode.getBindingPathArgument(domArg));
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

    NotificationCodecContext<?> getNotificationContext(final Absolute notification) {
        return root.getNotification(notification);
    }

    RpcInputCodec<?> getRpcInputCodec(final Absolute containerPath) {
        return root.getRpc(containerPath);
    }

    ActionCodecContext getActionCodec(final Class<? extends Action<?, ?, ?>> action) {
        return root.getAction(action);
    }

    @Override
    public ImmutableMap<Method, ValueNodeCodecContext> getLeafNodes(final Class<?> type,
            final EffectiveStatement<?, ?> schema) {
        final var getterToLeafSchema = new HashMap<String, DataSchemaNode>();
        for (var stmt : schema.effectiveSubstatements()) {
            if (stmt instanceof TypedDataSchemaNode typedSchema) {
                putLeaf(getterToLeafSchema, typedSchema);
            } else if (stmt instanceof AnydataSchemaNode anydataSchema) {
                putLeaf(getterToLeafSchema, anydataSchema);
            } else if (stmt instanceof AnyxmlSchemaNode anyxmlSchema) {
                putLeaf(getterToLeafSchema, anyxmlSchema);
            }
        }
        return getLeafNodesUsingReflection(type, getterToLeafSchema);
    }

    private static void putLeaf(final Map<String, DataSchemaNode> map, final DataSchemaNode leaf) {
        map.put(BindingSchemaMapping.getGetterMethodName(leaf), leaf);
    }

    private ImmutableMap<Method, ValueNodeCodecContext> getLeafNodesUsingReflection(
            final Class<?> parentClass, final Map<String, DataSchemaNode> getterToLeafSchema) {
        final var leaves = new HashMap<Method, ValueNodeCodecContext>();
        for (var method : parentClass.getMethods()) {
            // Only consider non-bridge methods with no arguments
            if (method.getParameterCount() == 0 && !method.isBridge()) {
                final DataSchemaNode schema = getterToLeafSchema.get(method.getName());

                final ValueNodeCodecContext valueNode;
                if (schema instanceof LeafSchemaNode leafSchema) {
                    // FIXME: MDSAL-670: this is not right as we need to find a concrete type, but this may return
                    //                   Object.class
                    final Class<?> valueType = method.getReturnType();
                    final ValueCodec<Object, Object> codec = getCodec(valueType, leafSchema.getType());
                    valueNode = LeafNodeCodecContext.of(leafSchema, codec, method.getName(), valueType,
                        context.getEffectiveModelContext());
                } else if (schema instanceof LeafListSchemaNode leafListSchema) {
                    final Optional<Type> optType = ClassLoaderUtils.getFirstGenericParameter(
                        method.getGenericReturnType());
                    checkState(optType.isPresent(), "Failed to find return type for %s", method);

                    final Class<?> valueType;
                    final Type genericType = optType.orElseThrow();
                    if (genericType instanceof Class<?> clazz) {
                        valueType = clazz;
                    } else if (genericType instanceof ParameterizedType parameterized) {
                        valueType = (Class<?>) parameterized.getRawType();
                    } else if (genericType instanceof WildcardType) {
                        // FIXME: MDSAL-670: this is not right as we need to find a concrete type
                        valueType = Object.class;
                    } else {
                        throw new IllegalStateException("Unexpected return type " + genericType);
                    }

                    final ValueCodec<Object, Object> codec = getCodec(valueType, leafListSchema.getType());
                    valueNode = new LeafSetNodeCodecContext(leafListSchema, codec, method.getName());
                } else if (schema instanceof AnyxmlSchemaNode anyxmlSchema) {
                    valueNode = new OpaqueNodeCodecContext.Anyxml<>(anyxmlSchema, method.getName(),
                            opaqueReturnType(method), loader);
                } else if (schema instanceof AnydataSchemaNode anydataSchema) {
                    valueNode = new OpaqueNodeCodecContext.Anydata<>(anydataSchema, method.getName(),
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
    ValueCodec<Object, Object> getCodec(final Class<?> valueType, final TypeDefinition<?> instantiatedType) {
        if (BaseIdentity.class.isAssignableFrom(valueType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final ValueCodec<Object, Object> casted = (ValueCodec) identityCodec;
            return casted;
        } else if (InstanceIdentifier.class.equals(valueType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final ValueCodec<Object, Object> casted = (ValueCodec) instanceIdentifierCodec;
            return casted;
        } else if (BindingReflections.isBindingClass(valueType)) {
            return getCodecForBindingClass(valueType, instantiatedType);
        }
        // FIXME: MDSAL-670: this is right for most situations, but we must never return NOOP_CODEC for
        //                   valueType=Object.class
        return SchemaUnawareCodec.NOOP_CODEC;
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    // FIXME: this is probably not right w.r.t. nulls
    private ValueCodec<Object, Object> getCodecForBindingClass(final Class<?> valueType,
            final TypeDefinition<?> typeDef) {
        if (typeDef instanceof IdentityrefTypeDefinition) {
            return new CompositeValueCodec.OfIdentity(valueType, identityCodec);
        } else if (typeDef instanceof InstanceIdentifierTypeDefinition) {
            return new CompositeValueCodec.OfInstanceIdentifier(valueType, instanceIdentifierCodec);
        } else if (typeDef instanceof UnionTypeDefinition unionType) {
            try {
                return UnionTypeCodec.of(valueType, unionType, this);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to load codec for " + valueType, e);
            }
        } else if (typeDef instanceof LeafrefTypeDefinition) {
            final var typeWithSchema = context.getTypeWithSchema(valueType);
            final var schema = typeWithSchema.statement();
            final TypeDefinition<?> def;
            if (schema instanceof TypeDefinitionAware typeDefAware) {
                def = typeDefAware.getTypeDefinition();
            } else if (schema instanceof TypeAware typeAware) {
                def = typeAware.getType();
            } else {
                throw new IllegalStateException("Unexpected schema " + schema);
            }
            return getCodec(valueType, def);
        }
        return SchemaUnawareCodec.of(valueType, typeDef);
    }

    @Override
    public IdentifiableItemCodec getPathArgumentCodec(final Class<?> listClz, final ListRuntimeType type) {
        final Optional<Class<Identifier<?>>> optIdentifier = ClassLoaderUtils.findFirstGenericArgument(listClz,
                Identifiable.class);
        checkState(optIdentifier.isPresent(), "Failed to find identifier for %s", listClz);

        final Class<Identifier<?>> identifier = optIdentifier.orElseThrow();
        final Map<QName, ValueContext> valueCtx = new HashMap<>();
        for (final ValueNodeCodecContext leaf : getLeafNodes(identifier, type.statement()).values()) {
            final QName name = leaf.getDomPathArgument().getNodeType();
            valueCtx.put(name, new ValueContext(identifier, leaf));
        }
        return IdentifiableItemCodec.of(type.statement(), identifier, listClz, valueCtx);
    }

    @Override
    public <E extends DataObject> CommonDataObjectCodecTreeNode<E> streamChild(final Class<E> childClass) {
        return root.streamChild(childClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Augmentation<?>> BindingAugmentationCodecTreeNode<A> getAugmentationCodec(
            final InstanceIdentifier<A> path) {
        final var codecContext = getCodecContextNode(path, null);
        if (codecContext instanceof BindingAugmentationCodecTreeNode) {
            return (BindingAugmentationCodecTreeNode<A>) codecContext;
        }
        throw new IllegalArgumentException(path + " does not refer to an Augmentation");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataObject> BindingDataObjectCodecTreeNode<T> getDataObjectCodec(
            final InstanceIdentifier<T> path) {
        final var codecContext = getCodecContextNode(path, null);
        if (codecContext instanceof BindingDataObjectCodecTreeNode) {
            return (BindingDataObjectCodecTreeNode<T>) codecContext;
        }
        throw new IllegalArgumentException(path + " does not refer to a plain DataObject");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataObject> CodecWithPath<T> getSubtreeCodecWithPath(final InstanceIdentifier<T> path) {
        final var yangArgs = new ArrayList<YangInstanceIdentifier.PathArgument>();
        final var codecContext = getCodecContextNode(path, yangArgs);

        // TODO Do we need defensive check here?
        return new CodecWithPath<>((CommonDataObjectCodecTreeNode<T>) codecContext,
            YangInstanceIdentifier.of(yangArgs));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataObject> CommonDataObjectCodecTreeNode<T> getSubtreeCodec(final InstanceIdentifier<T> path) {
        // TODO Do we need defensive check here?
        return (CommonDataObjectCodecTreeNode<T>) getCodecContextNode(path, null);
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
    public <A extends Augmentation<?>> AugmentationResult toNormalizedAugmentation(final InstanceIdentifier<A> path,
            final A data) {
        final var result = toNormalizedNode(path, data);
        if (result instanceof AugmentationResult augment) {
            return augment;
        }
        throw new IllegalArgumentException(path + " does not identify an Augmentation");
    }

    @Override
    public <T extends DataObject> NodeResult toNormalizedDataObject(final InstanceIdentifier<T> path, final T data) {
        final var result = toNormalizedNode(path, data);
        if (result instanceof NodeResult node) {
            return node;
        }
        throw new IllegalArgumentException(path + " does not identify a plain DataObject");
    }

    @Override
    public <T extends DataObject> NormalizedResult toNormalizedNode(final InstanceIdentifier<T> path, final T data) {
        // We create Binding Stream Writer which translates from Binding to Normalized Nodes
        final var yangArgs = new ArrayList<YangInstanceIdentifier.PathArgument>();
        final var codecContext = getCodecContextNode(path, yangArgs);
        final var yangPath = YangInstanceIdentifier.of(yangArgs);

        // We create DOM stream writer which produces normalized nodes
        final var result = new NormalizationResultHolder();
        final var domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final var bindingWriter = new BindingToNormalizedStreamWriter(codecContext, domWriter);
        final var augment = codecContext instanceof BindingAugmentationCodecTreeNode<?> augmentNode ? augmentNode
            : null;

        try {
            // Augmentations do not have a representation, so we are faking a ContainerNode as the parent and we will be
            // extracting the resulting children.
            if (augment != null) {
                domWriter.startContainerNode(FAKE_NODEID, NormalizedNodeStreamWriter.UNKNOWN_SIZE);
            }

            // We get serializer which reads binding data and uses Binding To Normalized Node writer to write result
            getSerializer(path.getTargetType()).serialize(data, bindingWriter);

            if (augment != null) {
                domWriter.endNode();
            }
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing path {} data {}", path, data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }

        // Terminate the fake container and extract it to the result
        if (augment != null) {
            return new AugmentationResult(yangPath, augment.childPathArguments(),
                ImmutableList.copyOf(((ContainerNode) result.getResult().data()).body()));
        }
        return new NodeResult(yangPath, result.getResult().data());
    }

    @Override
    public Entry<InstanceIdentifier<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode data) {
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
        final InstanceIdentifier<?> bindingPath = InstanceIdentifier.unsafeOf(builder);
        return Map.entry(bindingPath, lazyObj);
    }

    @Override
    public BaseNotification fromNormalizedNodeNotification(final Absolute path, final ContainerNode data) {
        return getNotificationContext(path).deserialize(data);
    }

    @Override
    public BaseNotification fromNormalizedNodeNotification(final Absolute path, final ContainerNode data,
            final Instant eventInstant) {
        return eventInstant == null ? fromNormalizedNodeNotification(path, data)
                : getNotificationContext(path).deserialize(data, eventInstant);
    }

    @Override
    public DataObject fromNormalizedNodeRpcData(final Absolute containerPath, final ContainerNode data) {
        return getRpcInputCodec(containerPath).deserialize(data);
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
    public ContainerNode toNormalizedNodeNotification(final Notification<?> data) {
        // FIXME: Should the cast to DataObject be necessary?
        return serializeDataObject((DataObject) data,
            (ctx, iface, domWriter) -> ctx.newNotificationWriter(
                (Class<? extends Notification<?>>) iface.asSubclass(Notification.class), domWriter));
    }

    @Override
    public ContainerNode toNormalizedNodeNotification(final Absolute path, final BaseNotification data) {
        checkArgument(data instanceof DataObject, "Unexpected data %s", data);
        @SuppressWarnings("rawtypes")
        final NotificationCodecContext notifContext = getNotificationContext(path);
        @SuppressWarnings("unchecked")
        final var result = notifContext.serialize((DataObject) data);
        verify(result instanceof ContainerNode, "Unexpected result %s from %s", result, data);
        return (ContainerNode) result;
    }

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    public ContainerNode toNormalizedNodeRpcData(final DataContainer data) {
        // FIXME: Should the cast to DataObject be necessary?
        return serializeDataObject((DataObject) data, BindingNormalizedNodeWriterFactory::newRpcWriter);
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
        final var result = new NormalizationResultHolder();
        // We create DOM stream writer which produces normalized nodes
        final var domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        final Class<? extends DataObject> type = data.implementedInterface();
        @SuppressWarnings("unchecked")
        final BindingStreamEventWriter writer = newWriter.createWriter(this, (Class<T>) type, domWriter);
        try {
            getSerializer(type).serialize(data, writer);
        } catch (final IOException e) {
            LOG.error("Unexpected failure while serializing data {}", data, e);
            throw new IllegalStateException("Failed to create normalized node", e);
        }
        return (ContainerNode) result.getResult().data();
    }


    private static boolean notBindingRepresentable(final NormalizedNode data) {
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
