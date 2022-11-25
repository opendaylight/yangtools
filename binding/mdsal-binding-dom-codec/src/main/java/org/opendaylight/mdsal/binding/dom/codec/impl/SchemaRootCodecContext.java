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
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.DataRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedListAction;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

final class SchemaRootCodecContext<D extends DataObject> extends DataContainerCodecContext<D, BindingRuntimeTypes> {

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

    private final LoadingCache<Class<? extends DataObject>, ChoiceNodeCodecContext<?>> choicesByClass =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public ChoiceNodeCodecContext<?> load(final Class<? extends DataObject> key) {
                return createChoiceDataContext(key);
            }
        });

    private final LoadingCache<Class<?>, NotificationCodecContext<?>> notificationsByClass = CacheBuilder.newBuilder()
        .build(new CacheLoader<Class<?>, NotificationCodecContext<?>>() {
            @Override
            public NotificationCodecContext<?> load(final Class<?> key) {
                checkArgument(key.isInterface(), "Supplied class must be interface.");

                // TODO: we should be able to work with bindingChild() instead of schemaTreeChild() here
                final QName qname = BindingReflections.findQName(key);
                final RuntimeType child = getType().schemaTreeChild(qname);
                checkArgument(child instanceof NotificationRuntimeType, "Supplied %s is not valid notification",
                    key);
                return new NotificationCodecContext<>(key, (NotificationRuntimeType) child, factory());
            }
        });

    private final LoadingCache<Class<?>, ContainerNodeCodecContext<?>> rpcDataByClass = CacheBuilder.newBuilder()
        .build(new CacheLoader<Class<?>, ContainerNodeCodecContext<?>>() {
            @Override
            public ContainerNodeCodecContext<?> load(final Class<?> key) {
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
                final String className = BindingMapping.getClassName(qname);

                for (final RpcDefinition potential : module.getRpcs()) {
                    final QName potentialQName = potential.getQName();
                    /*
                     * Check if rpc and class represents data from same module and then checks if rpc local name
                     * produces same class name as class name appended with Input/Output based on QName associated
                     * with binding class.
                     *
                     * FIXME: Rework this to have more precise logic regarding Binding Specification.
                     */
                    if (key.getSimpleName().equals(BindingMapping.getClassName(potentialQName) + className)) {
                        final ContainerLike schema = getRpcDataSchema(potential, qname);
                        checkArgument(schema != null, "Schema for %s does not define input / output.", potentialQName);

                        final ContainerLikeRuntimeType<?, ?> type = lookup.apply(context.getTypes(), potentialQName)
                            .orElseThrow(() -> new IllegalArgumentException("Cannot find runtime type for " + key));

                        return (ContainerNodeCodecContext) DataContainerCodecPrototype.from(key,
                            (ContainerLikeRuntimeType<?, ?>) type, factory).get();
                    }
                }

                throw new IllegalArgumentException("Supplied class " + key + " is not valid RPC class.");
            }
        });

    private final LoadingCache<QName, DataContainerCodecContext<?,?>> childrenByQName =
        CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public DataContainerCodecContext<?, ?> load(final QName qname) throws ClassNotFoundException {
                final var type = getType();
                final var child = childNonNull(type.schemaTreeChild(qname), qname,
                    "Argument %s is not valid child of %s", qname, type);
                if (!(child instanceof DataRuntimeType)) {
                    throw IncorrectNestingException.create("Argument %s is not valid data tree child of %s", qname,
                        type);
                }

                // TODO: improve this check?
                final var childSchema = child.statement();
                if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceSchemaNode) {
                    return streamChild(factory().getRuntimeContext().loadClass(child.javaType()));
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
                final Class<?> cls = factory().getRuntimeContext().getClassForSchema(key);
                checkArgument(Notification.class.isAssignableFrom(cls), "Path %s does not represent a notification",
                    key);
                return getNotificationImpl(cls);
            }
        });

    private SchemaRootCodecContext(final DataContainerCodecPrototype<BindingRuntimeTypes> dataPrototype) {
        super(dataPrototype);
    }

    /**
     * Creates RootNode from supplied CodecContextFactory.
     *
     * @param factory
     *            CodecContextFactory
     * @return A new root node
     */
    static SchemaRootCodecContext<?> create(final CodecContextFactory factory) {
        return new SchemaRootCodecContext<>(DataContainerCodecPrototype.rootPrototype(factory));
    }

    @Override
    public WithStatus getSchema() {
        return getType().getEffectiveModelContext();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends DataObject> DataContainerCodecContext<C, ?> streamChild(final Class<C> childClass) {
        final var result = Notification.class.isAssignableFrom(childClass) ? getNotificationImpl(childClass)
            : getOrRethrow(childrenByClass, childClass);
        return (DataContainerCodecContext<C, ?>) result;
    }

    @Override
    public <C extends DataObject> Optional<DataContainerCodecContext<C, ?>> possibleStreamChild(
            final Class<C> childClass) {
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

    ContainerNodeCodecContext<?> getRpc(final Class<? extends DataContainer> rpcInputOrOutput) {
        return getOrRethrow(rpcDataByClass, rpcInputOrOutput);
    }

    RpcInputCodec<?> getRpc(final Absolute containerPath) {
        return getOrRethrow(rpcDataByPath, containerPath);
    }

    DataContainerCodecContext<?, ?> createDataTreeChildContext(final Class<? extends DataObject> key) {
        final RuntimeType childSchema = childNonNull(getType().bindingChild(JavaTypeName.create(key)), key,
            "%s is not top-level item.", key);
        if (childSchema instanceof CompositeRuntimeType composite && childSchema instanceof DataRuntimeType) {
            return DataContainerCodecPrototype.from(key, composite, factory()).get();
        }
        throw IncorrectNestingException.create("%s is not a valid data tree child of %s", key, this);
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

        final ParameterizedType paramType = optParamType.get();
        final Type[] args = paramType.getActualTypeArguments();
        checkArgument(args.length == expectedArgsLength, "Unexpected (%s) Action generatic arguments", args.length);
        final ActionRuntimeType schema = factory().getRuntimeContext().getActionDefinition(action);
        return new ActionCodecContext(
            DataContainerCodecPrototype.from(asClass(args[inputOffset], RpcInput.class), schema.input(),
                factory()).get(),
            DataContainerCodecPrototype.from(asClass(args[outputOffset], RpcOutput.class), schema.output(),
                factory()).get());
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

    ChoiceNodeCodecContext<?> createChoiceDataContext(final Class<? extends DataObject> caseType) {
        final var choiceClass = findCaseChoice(caseType);
        if (choiceClass == null) {
            throw new IllegalArgumentException(caseType + " is not a valid case representation");
        }

        final var runtimeType = factory().getRuntimeContext().getSchemaDefinition(choiceClass);
        if (!(runtimeType instanceof ChoiceRuntimeType choiceType)) {
            throw new IllegalArgumentException(caseType + " does not refer to a choice");
        }

        final var choice = DataContainerCodecPrototype.from(choiceClass, choiceType, factory()).get();
        verify(choice instanceof ChoiceNodeCodecContext);
        return (ChoiceNodeCodecContext<?>) choice;
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
            final var caze = choice.streamChild(type);
            caze.addYangPathArgument(arg, builder);
            return caze.bindingPathArgumentChild(arg, builder);
        }

        return super.bindingPathArgumentChild(arg, builder);
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
