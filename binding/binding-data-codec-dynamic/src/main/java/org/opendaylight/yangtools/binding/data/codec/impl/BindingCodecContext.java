/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.base.VerifyException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.kohsuke.MetaInfServices;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.BaseNotification;
import org.opendaylight.yangtools.binding.BindingInstanceIdentifier;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyedListAction;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.OpaqueObject;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.contract.BuiltInType;
import org.opendaylight.yangtools.binding.data.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTree;
import org.opendaylight.yangtools.binding.data.codec.api.BindingCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingInstanceIdentifierCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeWriterFactory;
import org.opendaylight.yangtools.binding.data.codec.api.BindingStreamEventWriter;
import org.opendaylight.yangtools.binding.data.codec.api.BindingYangDataCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.IncorrectNestingException;
import org.opendaylight.yangtools.binding.data.codec.api.MissingSchemaException;
import org.opendaylight.yangtools.binding.data.codec.dynamic.DynamicBindingDataCodec;
import org.opendaylight.yangtools.binding.data.codec.spi.AbstractBindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingDOMCodecServices;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingSchemaMapping;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.reflect.BindingReflections;
import org.opendaylight.yangtools.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.DataRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.InputRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
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
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeAware;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionAware;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MetaInfServices(value = { BindingDataCodec.class, DynamicBindingDataCodec.class, BindingDOMCodecServices.class })
public final class BindingCodecContext extends AbstractBindingNormalizedNodeSerializer
        implements DynamicBindingDataCodec, CodecContextFactory, DataContainerSerializerRegistry, Immutable,
                   BindingDOMCodecServices {
    private static final Logger LOG = LoggerFactory.getLogger(BindingCodecContext.class);
    private static final @NonNull NodeIdentifier FAKE_NODEID = new NodeIdentifier(QName.create("fake", "fake"));
    private static final BindingClassLoader.@NonNull Builder BCL_BUILDER;

    static {
        final var builder = BindingClassLoader.builder(BindingCodecContext.class);
        final var dir = System.getProperty("org.opendaylight.mdsal.binding.dom.codec.loader.bytecodeDumpDirectory");
        if (dir != null && !dir.isEmpty()) {
            builder.dumpBytecode(Path.of(dir));
        }
        BCL_BUILDER = builder;
    }

    /**
     * A simple codec that just verifies against possible {@link BuiltInType}s.
     */
    // FIXME: YANGTOOLS-1602: this codec should not be needed
    @VisibleForTesting
    static final @NonNull SchemaUnawareCodec NOOP_CODEC = new SchemaUnawareCodec() {
        @Override
        protected Object serializeImpl(final Object input) {
            return BuiltInType.checkValue(input);
        }

        @Override
        protected Object deserializeImpl(final Object input) {
            return BuiltInType.checkValue(input);
        }
    };

    private final LoadingCache<Class<?>, DataContainerStreamer<?>> streamers = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
            @Override
            public DataContainerStreamer<?> load(final Class<?> key) throws ReflectiveOperationException {
                final var streamer = DataContainerStreamerGenerator.generateStreamer(loader, BindingCodecContext.this,
                    key);
                final var instance = streamer.getDeclaredField(DataContainerStreamerGenerator.INSTANCE_FIELD);
                return (DataContainerStreamer<?>) instance.get(null);
            }
        });
    private final LoadingCache<Class<?>, DataContainerSerializer> serializers = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
            @Override
            public DataContainerSerializer load(final Class<?> key) throws ExecutionException {
                return new DataContainerSerializer(BindingCodecContext.this, streamers.get(key));
            }
        });
    private final LoadingCache<Class<? extends DataObject>, DataContainerCodecContext<?, ?, ?>> childrenByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public DataContainerCodecContext<?, ?, ?> load(final Class<? extends DataObject> key) {
                final var childSchema = context.getTypes().bindingChild(JavaTypeName.create(key));
                if (childSchema instanceof ContainerLikeRuntimeType containerLike) {
                    if (childSchema instanceof ContainerRuntimeType container
                        && container.statement().findFirstEffectiveSubstatement(PresenceEffectiveStatement.class)
                            .isEmpty()) {
                        return new StructuralContainerCodecContext<>(key, container, BindingCodecContext.this);
                    }
                    return new ContainerLikeCodecContext<>(key, containerLike, BindingCodecContext.this);
                } else if (childSchema instanceof ListRuntimeType list) {
                    return list.keyType() == null ? new ListCodecContext<>(key, list, BindingCodecContext.this)
                        : MapCodecContext.of(key, list, BindingCodecContext.this);
                } else if (childSchema instanceof ChoiceRuntimeType choice) {
                    return new ChoiceCodecContext<>(key.asSubclass(ChoiceIn.class), choice, BindingCodecContext.this);
                } else if (childSchema == null) {
                    throw DataContainerCodecContext.childNullException(context, key, "%s is not top-level item.", key);
                } else {
                    throw new IncorrectNestingException("%s is not a valid data tree child of %s", key, this);
                }
            }
        });

    private final LoadingCache<@NonNull QName, CodecContext> childrenByDomArg =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public CodecContext load(final QName qname) throws ClassNotFoundException {
                final var type = context.getTypes();
                final var child = type.schemaTreeChild(qname);
                if (child == null) {
                    final var module = qname.getModule();
                    if (type.findModule(module).isEmpty()) {
                        throw new MissingSchemaException(
                            "Module " + module + " is not present in current schema context.");
                    }
                    throw new IncorrectNestingException("Argument %s is not valid child of %s", qname, type);
                }

                if (!(child instanceof DataRuntimeType)) {
                    throw new IncorrectNestingException("Argument %s is not valid data tree child of %s", qname, type);
                }

                // TODO: improve this check?
                final var childSchema = child.statement();
                if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceSchemaNode) {
                    return getStreamChild(context.loadClass(child.javaType()));
                } else if (childSchema instanceof AnydataSchemaNode || childSchema instanceof AnyxmlSchemaNode
                    || childSchema instanceof LeafSchemaNode || childSchema instanceof LeafListSchemaNode) {
                    final var module = type.findModule(qname.getModule()).orElseThrow();
                    final var moduleChildren = getLeafNodes(context.loadClass(module.javaType()), module.statement());
                    for (var moduleChild : moduleChildren.values()) {
                        if (qname.equals(moduleChild.getDomPathArgument().getNodeType())) {
                            return moduleChild;
                        }
                    }
                    throw new IllegalArgumentException("Failed to resolve " + child + " in " + module);
                } else {
                    throw new UnsupportedOperationException("Unsupported child type " + childSchema.getClass());
                }
            }
        });

    private final LoadingCache<Class<? extends DataObject>, ChoiceCodecContext<?>> choicesByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ChoiceCodecContext<?> load(final Class<? extends DataObject> caseType) {
                final var choiceClass = findCaseChoice(caseType);
                if (choiceClass == null) {
                    throw new IllegalArgumentException(caseType + " is not a valid case representation");
                }
                if (context.getSchemaDefinition(choiceClass) instanceof ChoiceRuntimeType choiceType) {
                    // FIXME: accurate type!
                    return new ChoiceCodecContext(choiceClass, choiceType, BindingCodecContext.this);
                }
                throw new IllegalArgumentException(caseType + " does not refer to a choice");
            }

            private static Class<?> findCaseChoice(final Class<? extends DataObject> caseClass) {
                for (var type : caseClass.getGenericInterfaces()) {
                    if (type instanceof Class<?> typeClass && ChoiceIn.class.isAssignableFrom(typeClass)) {
                        return typeClass.asSubclass(ChoiceIn.class);
                    }
                }
                return null;
            }
        });

    private final LoadingCache<Class<? extends Action<?, ?, ?>>, ActionCodecContext> actionsByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ActionCodecContext load(final Class<? extends Action<?, ?, ?>> action) {
                if (KeyedListAction.class.isAssignableFrom(action)) {
                    return prepareActionContext(2, 3, 4, action, KeyedListAction.class);
                } else if (Action.class.isAssignableFrom(action)) {
                    return prepareActionContext(1, 2, 3, action, Action.class);
                }
                throw new IllegalArgumentException("The specific action type does not exist for action "
                    + action.getName());
            }

            private ActionCodecContext prepareActionContext(final int inputOffset, final int outputOffset,
                    final int expectedArgsLength, final Class<? extends Action<?, ?, ?>> action,
                    final Class<?> actionType) {
                final var args = ClassLoaderUtils.findParameterizedType(action, actionType)
                    .orElseThrow(() -> new IllegalStateException(action + " does not specialize " + actionType))
                    .getActualTypeArguments();
                checkArgument(args.length == expectedArgsLength, "Unexpected (%s) Action generatic arguments",
                    args.length);
                final ActionRuntimeType schema = context.getActionDefinition(action);
                return new ActionCodecContext(
                    new ContainerLikeCodecContext(asClass(args[inputOffset], RpcInput.class), schema.input(),
                        BindingCodecContext.this),
                    new ContainerLikeCodecContext(asClass(args[outputOffset], RpcOutput.class), schema.output(),
                        BindingCodecContext.this));
            }

            private static <T extends DataObject> Class<? extends T> asClass(final Type type, final Class<T> target) {
                verify(type instanceof Class, "Type %s is not a class", type);
                return ((Class<?>) type).asSubclass(target);
            }
        });

    private final LoadingCache<Class<?>, NotificationCodecContext<?>> notificationsByClass = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
            @Override
            public NotificationCodecContext<?> load(final Class<?> key) {
                final var runtimeType = context.getTypes().bindingChild(JavaTypeName.create(key));
                if (runtimeType instanceof NotificationRuntimeType notification) {
                    return new NotificationCodecContext<>(key, notification, BindingCodecContext.this);
                } if (runtimeType != null) {
                    throw new IllegalArgumentException(key + " maps to unexpected " + runtimeType);
                }
                throw new IllegalArgumentException(key + " is not a known class");
            }
        });
    private final LoadingCache<Absolute, NotificationCodecContext<?>> notificationsByPath =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public NotificationCodecContext<?> load(final Absolute key) {
                final var cls = context.getClassForSchema(key);
                try {
                    return getNotificationContext(cls.asSubclass(Notification.class));
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Path " + key + " does not represent a notification", e);
                }
            }
        });

    private final LoadingCache<Class<?>, ContainerLikeCodecContext<?>> rpcDataByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ContainerLikeCodecContext<?> load(final Class<?> key) {
                final var runtimeType = context.getTypes().findSchema(JavaTypeName.create(key))
                    .orElseThrow(() -> new IllegalArgumentException(key + " is not a known class"));
                if (RpcInput.class.isAssignableFrom(key) && runtimeType instanceof InputRuntimeType input) {
                    // FIXME: accurate type
                    return new ContainerLikeCodecContext(key, input, BindingCodecContext.this);
                } else if (RpcOutput.class.isAssignableFrom(key) && runtimeType instanceof OutputRuntimeType output) {
                    // FIXME: accurate type
                    return new ContainerLikeCodecContext(key, output, BindingCodecContext.this);
                } else {
                    throw new IllegalArgumentException(key + " maps to unexpected " + runtimeType);
                }
            }
        });
    private final LoadingCache<Absolute, ContainerLikeCodecContext<?>> rpcDataByPath =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ContainerLikeCodecContext<?> load(final Absolute key) {
                final var rpcName = key.firstNodeIdentifier();

                final Class<? extends DataContainer> container = switch (key.lastNodeIdentifier().getLocalName()) {
                    case "input" -> context.getRpcInput(rpcName);
                    case "output" -> context.getRpcOutput(rpcName);
                    default -> throw new IllegalArgumentException("Unhandled path " + key);
                };

                return getRpc(container);
            }
        });
    private final LoadingCache<Class<? extends YangData<?>>, YangDataCodecContext<?>> yangDataByClass =
        CacheBuilder.newBuilder() .build(new CacheLoader<>() {
            @Override
            public YangDataCodecContext<?> load(final Class<? extends YangData<?>> key) {
                final var schema = context.getSchemaDefinition(key);
                if (schema instanceof YangDataRuntimeType yangData) {
                    return new YangDataCodecContext(key, yangData, BindingCodecContext.this);
                }
                throw new IllegalArgumentException(key + " maps to non-YangData " + schema);
            }
        });
    private final LoadingCache<YangDataName, BindingYangDataCodecTreeNode<?>> yangDataByName = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
            @Override
            public BindingYangDataCodecTreeNode<?> load(final YangDataName key) throws ExecutionException {
                return yangDataByClass.get(context.getYangDataClass(key));
            }
        });

    private final @NonNull BindingClassLoader loader = BCL_BUILDER.build();
    private final @NonNull InstanceIdentifierCodec instanceIdentifierCodec;
    private final @NonNull IdentityCodec identityCodec;
    private final @NonNull BindingRuntimeContext context;

    public BindingCodecContext() {
        this(ServiceLoader.load(BindingRuntimeContext.class).findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to load BindingRuntimeContext")));
    }

    public BindingCodecContext(final BindingRuntimeContext context) {
        this.context = requireNonNull(context, "Binding Runtime Context is required.");
        identityCodec = new IdentityCodec(context);
        instanceIdentifierCodec = new InstanceIdentifierCodec(this);
    }

    @Override
    @Deprecated(since = "14.0.2", forRemoval = true)
    public BindingRuntimeContext getRuntimeContext() {
        return context;
    }

    @Override
    public BindingRuntimeContext runtimeContext() {
        return context;
    }

    @Override
    public BindingNormalizedNodeSerializer nodeSerializer() {
        return this;
    }

    @Override
    public BindingCodecTree tree() {
        return this;
    }

    @Override
    public BindingNormalizedNodeWriterFactory writerFactory() {
        return this;
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
    @SuppressWarnings("unchecked")
    public <T extends YangData<T>> BindingYangDataCodecTreeNode<T> getYangDataCodec(final Class<T> yangDataClass) {
        return (BindingYangDataCodecTreeNode<T>) yangDataByClass.getUnchecked(requireNonNull(yangDataClass));
    }

    @Override
    public BindingYangDataCodecTreeNode<?> getYangDataCodec(final YangDataName yangDataName) {
        return yangDataByName.getUnchecked(requireNonNull(yangDataName));
    }

    @Override
    public DataContainerSerializer getEventStreamSerializer(final Class<?> type) {
        return serializers.getUnchecked(type);
    }

    @Override
    public DataContainerStreamer<?> getDataContainerStreamer(final Class<?> type) {
        return streamers.getUnchecked(type);
    }

    @Override
    public DataContainerSerializer getSerializer(final Class<? extends DataContainer> type) {
        return serializers.getUnchecked(type);
    }

    @Override
    public Entry<YangInstanceIdentifier, BindingStreamEventWriter> newWriterAndIdentifier(
            final DataObjectReference<?> path, final NormalizedNodeStreamWriter domWriter) {
        final var yangArgs = new ArrayList<PathArgument>();
        final var codecContext = getCodecContextNode(path, yangArgs);
        return Map.entry(YangInstanceIdentifier.of(yangArgs),
            new BindingToNormalizedStreamWriter(codecContext, domWriter));
    }

    @Override
    public BindingStreamEventWriter newWriter(final DataObjectReference<?> path,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getCodecContextNode(path, null), domWriter);
    }

    @Override
    public BindingStreamEventWriter newRpcWriter(final Class<? extends DataContainer> rpcInputOrOutput,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getRpc(rpcInputOrOutput), domWriter);
    }

    @Override
    public BindingStreamEventWriter newNotificationWriter(final Class<? extends Notification<?>> notification,
            final NormalizedNodeStreamWriter domWriter) {
        return new BindingToNormalizedStreamWriter(getNotificationContext(notification), domWriter);
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

    @NonNull DataContainerCodecContext<?, ?, ?> getCodecContextNode(final DataObjectReference<?> binding,
            final List<PathArgument> builder) {
        final var it = binding.steps().iterator();
        final var step = it.next();

        final DataContainerCodecContext<?, ?, ?> start;
        final var caseType = step.caseType();
        if (caseType != null) {
            final var choice = choicesByClass.getUnchecked(caseType);
            choice.addYangPathArgument(step, builder);
            final var caze = choice.getStreamChild(caseType);
            caze.addYangPathArgument(step, builder);
            start = caze.bindingPathArgumentChild(step, builder);
        } else {
            final var child = getStreamChild(step.type());
            child.addYangPathArgument(step, builder);
            start = child;
        }

        var current = start;
        while (it.hasNext()) {
            current = current.bindingPathArgumentChild(it.next(), builder);
        }
        return current;
    }

    /**
     * Multi-purpose utility function. Traverse the codec tree, looking for the appropriate codec for the specified
     * {@link YangInstanceIdentifier}. As a side-effect, gather all traversed binding {@link DataObjectStep}s into the
     * supplied collection.
     *
     * @param dom {@link YangInstanceIdentifier} which is to be translated
     * @param bindingArguments Collection for traversed path arguments
     * @return Codec for target node, or {@code null} if the node does not have a binding representation (choice, case,
     *         leaf).
     * @throws IllegalArgumentException if {@code dom} is empty
     */
    @Nullable BindingDataObjectCodecTreeNode<?> getCodecContextNode(final @NonNull YangInstanceIdentifier dom,
            final @Nullable List<DataObjectStep<?>> bindingArguments) {
        final var codec = lookupCodecContext(dom, bindingArguments);
        if (!(codec instanceof BindingDataObjectCodecTreeNode<?> dataObjectCodec)) {
            return null;
        }
        if (dataObjectCodec instanceof CaseCodecContext) {
            LOG.debug("Instance identifier targeting a case is not representable ({})", dom);
            return null;
        }
        return dataObjectCodec;
    }

    @Nullable CodecContext lookupCodecContext(final @NonNull YangInstanceIdentifier path,
            final @Nullable List<DataObjectStep<?>> bindingArguments) {
        final var it = path.getPathArguments().iterator();
        if (!it.hasNext()) {
            throw new IllegalArgumentException("Path may not be empty");
        }

        // First item is somewhat special:
        // 1. it has to be a NodeIdentifier, otherwise it is a malformed identifier and we do not find it
        var domArg = it.next();
        if (!(domArg instanceof NodeIdentifier)) {
            return null;
        }
        CodecContext nextNode = getOrRethrow(childrenByDomArg, domArg.getNodeType());

        CodecContext currentNode;
        switch (nextNode) {
            case ListCodecContext<?> listNode -> {
                // 2. if it is a list, we need to see if we are consuming another item.
                if (!it.hasNext()) {
                    // 2a: not further items: it boils down to a wildcard
                    if (bindingArguments != null) {
                        bindingArguments.add(listNode.getBindingPathArgument(null));
                    }
                    return listNode;
                }

                // 2b: there is a next item: it should either be a NodeIdentifier or a NodeIdentifierWithPredicates, but
                //     it has to have the same node type
                final var nextArg = it.next();
                if (nextArg instanceof NodeWithValue || !nextArg.getNodeType().equals(domArg.getNodeType())) {
                    throw new IllegalArgumentException("List should be referenced twice in " + path);
                }
                if (bindingArguments != null) {
                    bindingArguments.add(listNode.getBindingPathArgument(nextArg));
                }
                currentNode = listNode;
            }
            case ChoiceCodecContext<?> choiceNode -> {
                currentNode = choiceNode;
            }
            case CommonDataObjectCodecContext<?, ?> firstContainer -> {
                if (bindingArguments != null) {
                    bindingArguments.add(firstContainer.getBindingPathArgument(domArg));
                }
                currentNode = firstContainer;
            }
            case ValueNodeCodecContext valueNode -> {
                return checkValueCodec(valueNode, it);
            }
        }

        ListCodecContext<?> currentList = null;
        while (it.hasNext()) {
            domArg = it.next();
            if (!(currentNode instanceof DataContainerCodecContext previous)) {
                throw new IllegalArgumentException("Unexpected child of non-container node " + currentNode);
            }

            nextNode = previous.yangPathArgumentChild(domArg);

            /**
             * Compatibility case: if it's determined the node belongs to augmentation
             * then insert augmentation path argument in between.
             */
            if (nextNode instanceof AugmentationCodecContext<?> augmContext) {
                if (bindingArguments != null) {
                    bindingArguments.add(augmContext.bindingArg());
                }
                currentNode = nextNode;
                nextNode = augmContext.yangPathArgumentChild(domArg);
            }

            /*
             * List representation in YANG Instance Identifier consists of two arguments: first is list as a whole,
             * second is list as an item so if it is /list it means list as whole, if it is /list/list - it is
             * wildcarded and if it is /list/list[key] it is concrete item, all these variations are expressed in
             * DataObjectReference as am ExactDataObjectStep.
             */
            if (currentList != null) {
                checkArgument(currentList == nextNode, "List should be referenced two times in %s", path);

                // We entered list, so now we have all information to emit
                // list path using second list argument.
                if (bindingArguments != null) {
                    bindingArguments.add(currentList.getBindingPathArgument(domArg));
                }
                currentList = null;
                currentNode = nextNode;
            } else if (nextNode instanceof ListCodecContext<?> listNode) {
                // We enter list, we do not update current Node yet,
                // since we need to verify
                currentList = listNode;
            } else if (nextNode instanceof ChoiceCodecContext) {
                // We do not add path argument for choice, since
                // it is not supported by binding instance identifier.
                currentNode = nextNode;
            } else if (nextNode instanceof CommonDataObjectCodecContext<?, ?> containerNode) {
                if (bindingArguments != null) {
                    bindingArguments.add(containerNode.getBindingPathArgument(domArg));
                }
                currentNode = nextNode;
            } else if (nextNode instanceof ValueNodeCodecContext valueNode) {
                return checkValueCodec(valueNode, it);
            }
        }

        // Algorithm ended in list as whole representation, we still need to emit identifier for list
        if (currentList != null) {
            if (bindingArguments != null) {
                bindingArguments.add(currentList.getBindingPathArgument(null));
            }
            return currentList;
        }
        return currentNode;
    }

    private static ValueNodeCodecContext checkValueCodec(final ValueNodeCodecContext codec,
            final Iterator<PathArgument> it) {
        if (codec instanceof LeafSetNodeCodecContext leafSet && it.hasNext()) {
            final var nextArg = it.next();
            if (!(nextArg instanceof NodeWithValue)) {
                throw new IllegalArgumentException(nextArg + " should be a NodeWithValue matching " + leafSet);
            }
        }
        if (it.hasNext()) {
            throw new IllegalArgumentException(
                "Attempted to step " + ImmutableList.copyOf(it) + " past " + codec);
        }
        return codec;
    }

    NotificationCodecContext<?> getNotificationContext(final Absolute notification) {
        return getOrRethrow(notificationsByPath, notification);
    }

    private NotificationCodecContext<?> getNotificationContext(final Class<?> notification) {
        return getOrRethrow(notificationsByClass, notification);
    }

    ContainerLikeCodecContext<?> getRpc(final Class<? extends DataContainer> rpcInputOrOutput) {
        return getOrRethrow(rpcDataByClass, rpcInputOrOutput);
    }

    ContainerLikeCodecContext<?> getRpcInputCodec(final Absolute containerPath) {
        return getOrRethrow(rpcDataByPath, containerPath);
    }

    ActionCodecContext getActionCodec(final Class<? extends Action<?, ?, ?>> action) {
        return getOrRethrow(actionsByClass, action);
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
                    // FIXME: YANGTOOLS-1602: this is not right as we need to find a concrete type, but this may return
                    //                   Object.class
                    final Class<?> valueType = method.getReturnType();
                    final ValueCodec<Object, Object> codec = getCodec(valueType, leafSchema.getType());
                    valueNode = LeafNodeCodecContext.of(leafSchema, codec, method.getName(), valueType,
                        context.modelContext());
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
                        // FIXME: YANGTOOLS-1602: this is not right as we need to find a concrete type
                        valueType = Object.class;
                    } else {
                        throw new IllegalStateException("Unexpected return type " + genericType);
                    }

                    final ValueCodec<Object, Object> codec = getCodec(valueType, leafListSchema.getType());
                    valueNode = new LeafSetNodeCodecContext(leafListSchema, codec, method.getName(), valueType);
                } else if (schema instanceof AnyxmlSchemaNode anyxmlSchema) {
                    valueNode = new AnyxmlCodecContext<>(anyxmlSchema, method.getName(), opaqueReturnType(method),
                        loader);
                } else if (schema instanceof AnydataSchemaNode anydataSchema) {
                    valueNode = new AnydataCodecContext<>(anydataSchema, method.getName(), opaqueReturnType(method),
                        loader);
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
        } else if (BindingInstanceIdentifier.class.equals(valueType)) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final ValueCodec<Object, Object> casted = (ValueCodec) instanceIdentifierCodec;
            return casted;
        } else if (BindingReflections.isBindingClass(valueType)) {
            return getCodecForBindingClass(valueType, instantiatedType);
        } else {
            final var codec = BuiltInValueCodec.forValueType(valueType);
            if (codec != null) {
                return codec;
            }

            // FIXME: YANGTOOLS-1602: we must never return NOOP_CODEC for valueType=Object.class
            if (Object.class.equals(valueType)) {
                return NOOP_CODEC;
            }

            throw new VerifyException("Unsupported type " + valueType.getName());
        }
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
            final var schema = context.getTypeWithSchema(valueType).statement();
            return getCodec(valueType, switch (schema) {
                case TypeDefinitionAware typeDefAware -> typeDefAware.getTypeDefinition();
                case TypeAware typeAware -> typeAware.getType();
                default -> throw new IllegalStateException("Unexpected schema " + schema);
            });
        }

        final var cached = SchemaUnawareCodec.of(valueType, typeDef);
        if (cached != null) {
            return cached;
        }
        final var codec = BuiltInValueCodec.forValueType(valueType);
        if (codec != null) {
            return codec;
        }

        throw new VerifyException("Unsupported type " + valueType.getName());
    }

    @Override
    public IdentifiableItemCodec getPathArgumentCodec(final Class<?> listClz, final ListRuntimeType type) {
        @SuppressWarnings("unchecked")
        final Class<? extends Key<?>> identifier = (Class<? extends Key<?>>)
            ClassLoaderUtils.findGenericArgument(listClz, EntryObject.class, 1)
                .orElseThrow(() -> new IllegalStateException("Failed to find identifier for " + listClz))
                .asSubclass(Key.class);

        final var valueCtx = new HashMap<QName, ValueContext>();
        for (var leaf : getLeafNodes(identifier, type.statement()).values()) {
            final var name = leaf.getDomPathArgument().getNodeType();
            valueCtx.put(name, new ValueContext(identifier, leaf));
        }
        return IdentifiableItemCodec.of(type.statement(), identifier, listClz, valueCtx);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends DataObject> DataContainerCodecContext<E, ?, ?> getStreamChild(final Class<E> childClass) {
        final var result = Notification.class.isAssignableFrom(childClass) ? getNotificationContext(childClass)
            : getOrRethrow(childrenByClass, childClass);
        return (DataContainerCodecContext<E, ?, ?>) result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Augmentation<?>> BindingAugmentationCodecTreeNode<A> getAugmentationCodec(
            final DataObjectReference<A> path) {
        final var codecContext = getCodecContextNode(path, null);
        if (codecContext instanceof BindingAugmentationCodecTreeNode) {
            return (BindingAugmentationCodecTreeNode<A>) codecContext;
        }
        throw new IllegalArgumentException(path + " does not refer to an Augmentation");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataObject> BindingDataObjectCodecTreeNode<T> getDataObjectCodec(
            final DataObjectReference<T> path) {
        final var codecContext = getCodecContextNode(path, null);
        if (codecContext instanceof BindingDataObjectCodecTreeNode) {
            return (BindingDataObjectCodecTreeNode<T>) codecContext;
        }
        throw new IllegalArgumentException(path + " does not refer to a plain DataObject");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataObject> CodecWithPath<T> getSubtreeCodecWithPath(final DataObjectReference<T> path) {
        final var yangArgs = new ArrayList<PathArgument>();
        final var codecContext = getCodecContextNode(path, yangArgs);

        // TODO Do we need defensive check here?
        return new CodecWithPath<>((CommonDataObjectCodecTreeNode<T>) codecContext,
            YangInstanceIdentifier.of(yangArgs));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataObject> CommonDataObjectCodecTreeNode<T> getSubtreeCodec(final DataObjectReference<T> path) {
        // TODO Do we need defensive check here?
        return (CommonDataObjectCodecTreeNode<T>) getCodecContextNode(path, null);
    }

    @Override
    public BindingCodecTreeNode getSubtreeCodec(final YangInstanceIdentifier path) {
        return lookupCodecContext(requireNonNull(path), null);
    }

    @Override
    public BindingCodecTreeNode getSubtreeCodec(final Absolute path) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public YangInstanceIdentifier toYangInstanceIdentifier(final DataObjectReference<?> binding) {
        return instanceIdentifierCodec.fromBinding(binding);
    }

    @Override
    public <T extends DataObject> DataObjectReference<T> fromYangInstanceIdentifier(final YangInstanceIdentifier dom) {
        return instanceIdentifierCodec.toBinding(dom);
    }

    @Override
    public <A extends Augmentation<?>> AugmentationResult toNormalizedAugmentation(final DataObjectReference<A> path,
            final A data) {
        final var result = toNormalizedNode(path, data);
        if (result instanceof AugmentationResult augment) {
            return augment;
        }
        throw new IllegalArgumentException(path + " does not identify an Augmentation");
    }

    @Override
    public <T extends DataObject> NodeResult toNormalizedDataObject(final DataObjectReference<T> path, final T data) {
        final var result = toNormalizedNode(path, data);
        if (result instanceof NodeResult node) {
            return node;
        }
        throw new IllegalArgumentException(path + " does not identify a plain DataObject");
    }

    @Override
    public <T extends DataObject> NormalizedResult toNormalizedNode(final DataObjectReference<T> path, final T data) {
        // We create Binding Stream Writer which translates from Binding to Normalized Nodes
        final var yangArgs = new ArrayList<PathArgument>();
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
            getSerializer(path.lastStep().type()).serialize(data, bindingWriter);

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
    public Entry<DataObjectReference<?>, DataObject> fromNormalizedNode(final YangInstanceIdentifier path,
            final NormalizedNode data) {
        if (notBindingRepresentable(data)) {
            return null;
        }

        final var builder = new ArrayList<DataObjectStep<?>>();
        final var codec = getCodecContextNode(path, builder);
        if (codec == null) {
            if (data != null) {
                LOG.warn("Path {} does not have a binding equivalent, should have been caught earlier ({})", path,
                    data.getClass());
            }
            return null;
        }

        final DataObject lazyObj = codec.deserialize(data);
        return Map.entry(DataObjectReference.ofUnsafeSteps(builder), lazyObj);
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

    @Override
    protected NodeIdentifier actionInputName(final Class<? extends Action<?, ?, ?>> action) {
        return verifyNotNull(getActionCodec(action).input().getDomPathArgument());
    }

    @Override
    protected NodeIdentifier actionOutputName(final Class<? extends Action<?, ?, ?>> action) {
        return verifyNotNull(getActionCodec(action).output().getDomPathArgument());
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

    private static <K,V> V getOrRethrow(final LoadingCache<K, V> cache, final K key) {
        try {
            return cache.getUnchecked(key);
        } catch (UncheckedExecutionException e) {
            final var cause = e.getCause();
            if (cause != null) {
                Throwables.throwIfUnchecked(cause);
            }
            throw e;
        }
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
