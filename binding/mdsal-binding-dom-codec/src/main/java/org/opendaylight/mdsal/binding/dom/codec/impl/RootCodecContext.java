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

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableCollection;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.DataRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedListAction;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class RootCodecContext<D extends DataObject> extends DataContainerCodecContext<D, BindingRuntimeTypes>
        implements BindingDataObjectCodecTreeNode<D> {
    /**
     * Prototype for the root of YANG modeled world. This class only exists because DataContainerCodecContext requires
     * a prototype.
     */
    static final class Prototype extends DataObjectCodecPrototype<BindingRuntimeTypes> {
        private static final @NonNull NodeIdentifier ROOT_NODEID = NodeIdentifier.create(SchemaContext.NAME);

        private Prototype(final CodecContextFactory factory) {
            super(DataRoot.class, ROOT_NODEID, factory.getRuntimeContext().getTypes(), factory);
        }

        @Override
        RootCodecContext<?> createInstance() {
            throw new UnsupportedOperationException("Should never be invoked");
        }
    }

    private final LoadingCache<Class<? extends DataObject>, DataContainerCodecContext<?, ?>> childrenByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public DataContainerCodecContext<?, ?> load(final Class<? extends DataObject> key) {
                return createDataTreeChildContext(key);
            }
        });

    private final LoadingCache<Class<? extends Action<?, ?, ?>>, ActionCodecContext> actionsByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ActionCodecContext load(final Class<? extends Action<?, ?, ?>> key) {
                return createActionContext(key);
            }
        });

    private final LoadingCache<Class<? extends DataObject>, ChoiceCodecContext<?>> choicesByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ChoiceCodecContext<?> load(final Class<? extends DataObject> key) {
                return createChoiceDataContext(key);
            }
        });

    private final LoadingCache<Class<?>, NotificationCodecContext<?>> notificationsByClass = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
            @Override
            public NotificationCodecContext<?> load(final Class<?> key) {
                // FIXME: sharpen check to an Notification.class
                checkArgument(key.isInterface(), "Supplied class must be interface.");

                // TODO: we should be able to work with bindingChild() instead of schemaTreeChild() here
                final var qname = BindingReflections.findQName(key);
                if (type().schemaTreeChild(qname) instanceof NotificationRuntimeType type) {
                    return new NotificationCodecContext<>(key, type, factory());
                }
                throw new IllegalArgumentException("Supplied " + key + " is not valid notification");
            }
        });

    private final LoadingCache<Class<?>, ContainerLikeCodecContext<?>> rpcDataByClass = CacheBuilder.newBuilder()
        .build(new CacheLoader<>() {
            @Override
            public ContainerLikeCodecContext<?> load(final Class<?> key) {
                final BiFunction<BindingRuntimeTypes, QName, Optional<? extends ContainerLikeRuntimeType<?, ?>>> lookup;
                if (RpcInput.class.isAssignableFrom(key)) {
                    lookup = BindingRuntimeTypes::findRpcInput;
                } else if (RpcOutput.class.isAssignableFrom(key)) {
                    lookup = BindingRuntimeTypes::findRpcOutput;
                } else {
                    throw new IllegalArgumentException(key + " does not represent an RPC container");
                }

                final CodecContextFactory factory = factory();
                final BindingRuntimeContext context = factory.getRuntimeContext();

                final QName qname = BindingReflections.findQName(key);
                final QNameModule qnameModule = qname.getModule();
                final Module module = context.getEffectiveModelContext().findModule(qnameModule)
                    .orElseThrow(() -> new IllegalArgumentException("Failed to find module for " + qnameModule));
                final String className = Naming.getClassName(qname);

                for (final RpcDefinition potential : module.getRpcs()) {
                    final QName potentialQName = potential.getQName();
                    /*
                     * Check if rpc and class represents data from same module and then checks if rpc local name
                     * produces same class name as class name appended with Input/Output based on QName associated
                     * with binding class.
                     *
                     * FIXME: Rework this to have more precise logic regarding Binding Specification.
                     */
                    if (key.getSimpleName().equals(Naming.getClassName(potentialQName) + className)) {
                        final ContainerLike schema = getRpcDataSchema(potential, qname);
                        checkArgument(schema != null, "Schema for %s does not define input / output.", potentialQName);

                        final var type = lookup.apply(context.getTypes(), potentialQName)
                            .orElseThrow(() -> new IllegalArgumentException("Cannot find runtime type for " + key));

                        // FIXME: accurate type
                        return new ContainerLikeCodecContext(key, type, factory);
                    }
                }

                throw new IllegalArgumentException("Supplied class " + key + " is not valid RPC class.");
            }
        });

    private final LoadingCache<QName, DataContainerCodecContext<?,?>> childrenByQName =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public DataContainerCodecContext<?, ?> load(final QName qname) throws ClassNotFoundException {
                final var type = type();
                final var child = childNonNull(type.schemaTreeChild(qname), qname,
                    "Argument %s is not valid child of %s", qname, type);
                if (!(child instanceof DataRuntimeType)) {
                    throw new IncorrectNestingException("Argument %s is not valid data tree child of %s", qname, type);
                }

                // TODO: improve this check?
                final var childSchema = child.statement();
                if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceSchemaNode) {
                    return getStreamChild(factory().getRuntimeContext().loadClass(child.javaType()));
                }

                throw new UnsupportedOperationException("Unsupported child type " + childSchema.getClass());
            }
        });

    private final LoadingCache<Absolute, RpcInputCodec<?>> rpcDataByPath =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public RpcInputCodec<?> load(final Absolute key) {
                final var rpcName = key.firstNodeIdentifier();
                final var context = factory().getRuntimeContext();

                final Class<? extends DataContainer> container = switch (key.lastNodeIdentifier().getLocalName()) {
                    case "input" -> context.getRpcInput(rpcName);
                    case "output" -> context.getRpcOutput(rpcName);
                    default -> throw new IllegalArgumentException("Unhandled path " + key);
                };

                return getRpc(container);
            }
        });

    private final LoadingCache<Absolute, NotificationCodecContext<?>> notificationsByPath =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public NotificationCodecContext<?> load(final Absolute key) {
                final var cls = factory().getRuntimeContext().getClassForSchema(key);
                try {
                    return getNotificationImpl(cls.asSubclass(Notification.class));
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Path " + key + " does not represent a notification", e);
                }
            }
        });

    RootCodecContext(final CodecContextFactory factory) {
        super(new Prototype(factory));
    }

    @Override
    public WithStatus getSchema() {
        return type().getEffectiveModelContext();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends DataObject> DataContainerCodecContext<C, ?> getStreamChild(final Class<C> childClass) {
        final var result = Notification.class.isAssignableFrom(childClass) ? getNotificationImpl(childClass)
            : getOrRethrow(childrenByClass, childClass);
        return (DataContainerCodecContext<C, ?>) result;
    }

    @Override
    public <C extends DataObject> DataContainerCodecContext<C, ?> streamChild(final Class<C> childClass) {
        // FIXME: implement this
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DataContainerCodecContext<?,?> yangPathArgumentChild(final PathArgument arg) {
        return getOrRethrow(childrenByQName, arg.getNodeType());
    }

    @Override
    public D deserialize(final NormalizedNode data) {
        throw new UnsupportedOperationException("Could not create Binding data representation for root");
    }

    @Override
    public NormalizedNode serialize(final D data) {
        return serializeImpl(data);
    }

    ActionCodecContext getAction(final Class<? extends Action<?, ?, ?>> action) {
        return getOrRethrow(actionsByClass, action);
    }

    NotificationCodecContext<?> getNotification(final Absolute notification) {
        return getOrRethrow(notificationsByPath, notification);
    }

    NotificationCodecContext<?> getNotification(final Class<? extends Notification<?>> notification) {
        return getNotificationImpl(notification);
    }

    private NotificationCodecContext<?> getNotificationImpl(final Class<?> notification) {
        return getOrRethrow(notificationsByClass, notification);
    }

    ContainerLikeCodecContext<?> getRpc(final Class<? extends DataContainer> rpcInputOrOutput) {
        return getOrRethrow(rpcDataByClass, rpcInputOrOutput);
    }

    RpcInputCodec<?> getRpc(final Absolute containerPath) {
        return getOrRethrow(rpcDataByPath, containerPath);
    }

    DataContainerCodecContext<?, ?> createDataTreeChildContext(final Class<? extends DataObject> key) {
        final var childSchema = childNonNull(type().bindingChild(JavaTypeName.create(key)), key,
            "%s is not top-level item.", key);
        if (childSchema instanceof ContainerLikeRuntimeType containerLike) {
            if (childSchema instanceof ContainerRuntimeType container
                && container.statement().findFirstEffectiveSubstatement(PresenceEffectiveStatement.class).isEmpty()) {
                return new StructuralContainerCodecContext<>(key, container, factory());
            }
            return new ContainerLikeCodecContext<>(key, containerLike, factory());
        } else if (childSchema instanceof ListRuntimeType list) {
            return list.keyType() == null ? new ListCodecContext<>(key, list, factory())
                : MapCodecContext.of(key, list, factory());
        } else if (childSchema instanceof ChoiceRuntimeType choice) {
            return new ChoiceCodecContext<>(key, choice, factory());
        }
        throw new IncorrectNestingException("%s is not a valid data tree child of %s", key, this);
    }

    ActionCodecContext createActionContext(final Class<? extends Action<?, ?, ?>> action) {
        if (KeyedListAction.class.isAssignableFrom(action)) {
            return prepareActionContext(2, 3, 4, action, KeyedListAction.class);
        } else if (Action.class.isAssignableFrom(action)) {
            return prepareActionContext(1, 2, 3, action, Action.class);
        }
        throw new IllegalArgumentException("The specific action type does not exist for action " + action.getName());
    }

    private ActionCodecContext prepareActionContext(final int inputOffset, final int outputOffset,
            final int expectedArgsLength, final Class<? extends Action<?, ?, ?>> action, final Class<?> actionType) {
        final Optional<ParameterizedType> optParamType = ClassLoaderUtils.findParameterizedType(action, actionType);
        checkState(optParamType.isPresent(), "%s does not specialize %s", action, actionType);

        final ParameterizedType paramType = optParamType.orElseThrow();
        final Type[] args = paramType.getActualTypeArguments();
        checkArgument(args.length == expectedArgsLength, "Unexpected (%s) Action generatic arguments", args.length);
        final ActionRuntimeType schema = factory().getRuntimeContext().getActionDefinition(action);
        return new ActionCodecContext(
            new ContainerLikeCodecContext(asClass(args[inputOffset], RpcInput.class), schema.input(), factory()),
            new ContainerLikeCodecContext(asClass(args[outputOffset], RpcOutput.class), schema.output(), factory()));
    }

    private static <T extends DataObject> Class<? extends T> asClass(final Type type, final Class<T> target) {
        verify(type instanceof Class, "Type %s is not a class", type);
        return ((Class<?>) type).asSubclass(target);
    }

    /**
     * Returns RPC input or output schema based on supplied QName.
     *
     * @param rpc RPC Definition
     * @param qname input or output QName with namespace same as RPC
     * @return input or output schema. Returns null if RPC does not have input/output specified.
     */
    private static @Nullable ContainerLike getRpcDataSchema(final @NonNull RpcDefinition rpc,
            final @NonNull QName qname) {
        requireNonNull(rpc, "Rpc Schema must not be null");
        return switch (requireNonNull(qname, "QName must not be null").getLocalName()) {
            case "input" -> rpc.getInput();
            case "output" -> rpc.getOutput();
            default -> throw new IllegalArgumentException(
                "Supplied qname " + qname + " does not represent rpc input or output.");
        };
    }

    ChoiceCodecContext<?> createChoiceDataContext(final Class<? extends DataObject> caseType) {
        final var choiceClass = findCaseChoice(caseType);
        if (choiceClass == null) {
            throw new IllegalArgumentException(caseType + " is not a valid case representation");
        }

        final var runtimeType = factory().getRuntimeContext().getSchemaDefinition(choiceClass);
        if (!(runtimeType instanceof ChoiceRuntimeType choiceType)) {
            throw new IllegalArgumentException(caseType + " does not refer to a choice");
        }

        // FIXME: accurate type!
        return new ChoiceCodecContext(choiceClass, choiceType, factory());
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        throw new UnsupportedOperationException("Unable to deserialize root");
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        checkArgument(arg == null);
        return null;
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        checkArgument(arg == null);
        return null;
    }

    @Override
    public DataContainerCodecContext<?, ?> bindingPathArgumentChild(final InstanceIdentifier.PathArgument arg,
            final List<PathArgument> builder) {
        final var caseType = arg.getCaseType();
        if (caseType.isPresent()) {
            final @NonNull Class<? extends DataObject> type = caseType.orElseThrow();
            final var choice = choicesByClass.getUnchecked(type);
            choice.addYangPathArgument(arg, builder);
            final var caze = choice.getStreamChild(type);
            caze.addYangPathArgument(arg, builder);
            return caze.bindingPathArgumentChild(arg, builder);
        }

        return super.bindingPathArgumentChild(arg, builder);
    }

    @Override
    public BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            final ImmutableCollection<Class<? extends BindingObject>> cacheSpecifier) {
        return createCachingCodec(this, cacheSpecifier);
    }

    private static Class<?> findCaseChoice(final Class<? extends DataObject> caseClass) {
        for (var type : caseClass.getGenericInterfaces()) {
            if (type instanceof Class<?> typeClass && ChoiceIn.class.isAssignableFrom(typeClass)) {
                return typeClass.asSubclass(ChoiceIn.class);
            }
        }
        return null;
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
}
