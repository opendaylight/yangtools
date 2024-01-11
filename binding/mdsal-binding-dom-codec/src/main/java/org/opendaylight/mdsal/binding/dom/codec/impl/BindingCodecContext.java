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
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
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
import org.opendaylight.mdsal.binding.dom.codec.api.BindingYangDataCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.dom.codec.api.MissingSchemaException;
import org.opendaylight.mdsal.binding.dom.codec.spi.AbstractBindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingDOMCodecServices;
import org.opendaylight.mdsal.binding.dom.codec.spi.BindingSchemaMapping;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.DataRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BaseNotification;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Key;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.binding.KeyedListAction;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.binding.YangData;
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

@MetaInfServices(value = BindingDOMCodecServices.class)
public final class BindingCodecContext extends AbstractBindingNormalizedNodeSerializer
        implements BindingDOMCodecServices, Immutable, CodecContextFactory, DataContainerSerializerRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(BindingCodecContext.class);
    private static final @NonNull NodeIdentifier FAKE_NODEID = new NodeIdentifier(QName.create("fake", "fake"));
    private static final File BYTECODE_DIRECTORY;

    static {
        final String dir = System.getProperty("org.opendaylight.mdsal.binding.dom.codec.loader.bytecodeDumpDirectory");
        BYTECODE_DIRECTORY = Strings.isNullOrEmpty(dir) ? null : new File(dir);
    }

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

    // FIXME: this could also be a leaf!
    private final LoadingCache<QName, DataContainerCodecContext<?, ?, ?>> childrenByDomArg =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public DataContainerCodecContext<?, ?, ?> load(final QName qname) throws ClassNotFoundException {
                final var type = context.getTypes();
                final var child = type.schemaTreeChild(qname);
                if (child == null) {
                    final var module = qname.getModule();
                    if (context.modelContext().findModule(module).isEmpty()) {
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
                }

                throw new UnsupportedOperationException("Unsupported child type " + childSchema.getClass());
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
    private final LoadingCache<Absolute, RpcInputCodec<?>> rpcDataByPath =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public RpcInputCodec<?> load(final Absolute key) {
                final var rpcName = key.firstNodeIdentifier();

                final Class<? extends DataContainer> container = switch (key.lastNodeIdentifier().getLocalName()) {
                    case "input" -> context.getRpcInput(rpcName);
                    case "output" -> context.getRpcOutput(rpcName);
                    default -> throw new IllegalArgumentException("Unhandled path " + key);
                };

                return getRpc(container);
            }
        });

    private final @NonNull BindingClassLoader loader =
        BindingClassLoader.create(BindingCodecContext.class, BYTECODE_DIRECTORY);
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
    public <T extends YangData<T>> BindingYangDataCodecTreeNode<T> getYangDataCodec(final Class<T> yangDataClass) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public BindingYangDataCodecTreeNode<?> getYangDataCodec(final YangDataName yangDataName) {
        throw new UnsupportedOperationException("Not implemented yet");
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
            final InstanceIdentifier<?> path, final NormalizedNodeStreamWriter domWriter) {
        final var yangArgs = new ArrayList<PathArgument>();
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

    @NonNull DataContainerCodecContext<?, ?, ?> getCodecContextNode(final InstanceIdentifier<?> binding,
            final List<PathArgument> builder) {
        final var it = binding.getPathArguments().iterator();
        final var arg = it.next();

        DataContainerCodecContext<?, ?, ?> current;
        final var caseType = arg.getCaseType();
        if (caseType.isPresent()) {
            final @NonNull Class<? extends DataObject> type = caseType.orElseThrow();
            final var choice = choicesByClass.getUnchecked(type);
            choice.addYangPathArgument(arg, builder);
            final var caze = choice.getStreamChild(type);
            caze.addYangPathArgument(arg, builder);
            current = caze.bindingPathArgumentChild(arg, builder);
        } else {
            final var child = getStreamChild(arg.getType());
            child.addYangPathArgument(arg, builder);
            current = child;
        }

        while (it.hasNext()) {
            current = current.bindingPathArgumentChild(it.next(), builder);
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
     * @return Codec for target node, or {@code null} if the node does not have a binding representation (choice, case,
     *         leaf).
     * @throws IllegalArgumentException if {@code dom} is empty
     */
    @Nullable BindingDataObjectCodecTreeNode<?> getCodecContextNode(final @NonNull YangInstanceIdentifier dom,
            final @Nullable Collection<InstanceIdentifier.PathArgument> bindingArguments) {
        final var it = dom.getPathArguments().iterator();
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
        if (nextNode instanceof ListCodecContext<?> listNode) {
            // 2. if it is a list, we need to see if we are consuming another item.
            if (!it.hasNext()) {
                // 2a: not further items: it boils down to a wildcard
                if (bindingArguments != null) {
                    bindingArguments.add(listNode.getBindingPathArgument(null));
                }
                return listNode;
            }

            // 2b: there is a next item: it should either be a NodeIdentifier or a NodeIdentifierWithPredicates, but it
            //     has to have the same node type
            final var nextArg = it.next();
            if (nextArg instanceof NodeWithValue || !nextArg.getNodeType().equals(domArg.getNodeType())) {
                throw new IllegalArgumentException(
                    "List should be referenced two times in YANG Instance Identifier " + dom);
            }
            if (bindingArguments != null) {
                bindingArguments.add(listNode.getBindingPathArgument(nextArg));
            }
            currentNode = nextNode;
        } else if (nextNode instanceof ChoiceCodecContext) {
            currentNode = nextNode;
        } else if (nextNode instanceof CommonDataObjectCodecContext<?, ?> firstContainer) {
            if (bindingArguments != null) {
                bindingArguments.add(firstContainer.getBindingPathArgument(domArg));
            }
            currentNode = nextNode;
        } else {
            return null;
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
             * wildcarded and if it is /list/list[key] it is concrete item, all this variations are expressed in
             * InstanceIdentifier as Item or IdentifiableItem
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
            } else if (nextNode instanceof ValueNodeCodecContext) {
                LOG.debug("Instance identifier referencing a leaf is not representable ({})", dom);
                return null;
            }
        }

        // Algorithm ended in list as whole representation
        // we sill need to emit identifier for list
        if (currentNode instanceof ChoiceCodecContext) {
            LOG.debug("Instance identifier targeting a choice is not representable ({})", dom);
            return null;
        }
        if (currentNode instanceof CaseCodecContext) {
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
        return getOrRethrow(notificationsByPath, notification);
    }

    private NotificationCodecContext<?> getNotificationContext(final Class<?> notification) {
        return getOrRethrow(notificationsByClass, notification);
    }

    ContainerLikeCodecContext<?> getRpc(final Class<? extends DataContainer> rpcInputOrOutput) {
        return getOrRethrow(rpcDataByClass, rpcInputOrOutput);
    }

    RpcInputCodec<?> getRpcInputCodec(final Absolute containerPath) {
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
                    // FIXME: MDSAL-670: this is not right as we need to find a concrete type, but this may return
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
                        // FIXME: MDSAL-670: this is not right as we need to find a concrete type
                        valueType = Object.class;
                    } else {
                        throw new IllegalStateException("Unexpected return type " + genericType);
                    }

                    final ValueCodec<Object, Object> codec = getCodec(valueType, leafListSchema.getType());
                    valueNode = new LeafSetNodeCodecContext(leafListSchema, codec, method.getName());
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
        final Optional<Class<Key<?>>> optIdentifier = ClassLoaderUtils.findFirstGenericArgument(listClz,
                KeyAware.class);
        checkState(optIdentifier.isPresent(), "Failed to find identifier for %s", listClz);

        final Class<Key<?>> identifier = optIdentifier.orElseThrow();
        final Map<QName, ValueContext> valueCtx = new HashMap<>();
        for (final ValueNodeCodecContext leaf : getLeafNodes(identifier, type.statement()).values()) {
            final QName name = leaf.getDomPathArgument().getNodeType();
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
        final var yangArgs = new ArrayList<PathArgument>();
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
        return getCodecContextNode(requireNonNull(path), null);
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

        final var builder = new ArrayList<InstanceIdentifier.PathArgument>();
        final var codec = getCodecContextNode(path, builder);
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
