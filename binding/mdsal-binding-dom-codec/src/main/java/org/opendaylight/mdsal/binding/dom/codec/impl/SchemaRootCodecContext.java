/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.base.Verify;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.lang.reflect.Type;
import java.util.List;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;

final class SchemaRootCodecContext<D extends DataObject> extends DataContainerCodecContext<D,SchemaContext> {

    private final LoadingCache<Class<?>, DataContainerCodecContext<?,?>> childrenByClass = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<?>, DataContainerCodecContext<?,?>>() {
                @Override
                public DataContainerCodecContext<?,?> load(final Class<?> key) {
                    return createDataTreeChildContext(key);
                }
            });

    private final LoadingCache<Class<?>, ContainerNodeCodecContext<?>> rpcDataByClass = CacheBuilder.newBuilder().build(
            new CacheLoader<Class<?>, ContainerNodeCodecContext<?>>() {
                @Override
                public ContainerNodeCodecContext<?> load(final Class<?> key) {
                    return createRpcDataContext(key);
                }
            });

    private final LoadingCache<Class<?>, NotificationCodecContext<?>> notificationsByClass = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<?>, NotificationCodecContext<?>>() {
                @Override
                public NotificationCodecContext<?> load(final Class<?> key) {
                    return createNotificationDataContext(key);
                }
            });

    private final LoadingCache<Class<? extends DataObject>, ChoiceNodeCodecContext<?>> choicesByClass =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends DataObject>, ChoiceNodeCodecContext<?>>() {
                @Override
                public ChoiceNodeCodecContext<?> load(final Class<? extends DataObject> key) {
                    return createChoiceDataContext(key);
                }
            });

    private final LoadingCache<QName, DataContainerCodecContext<?,?>> childrenByQName = CacheBuilder.newBuilder().build(
            new CacheLoader<QName, DataContainerCodecContext<?,?>>() {
                @Override
                public DataContainerCodecContext<?,?> load(final QName qname) {
                    final DataSchemaNode childSchema = getSchema().getDataChildByName(qname);
                    childNonNull(childSchema, qname,"Argument %s is not valid child of %s", qname,getSchema());
                    if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceSchemaNode) {
                        @SuppressWarnings("unchecked")
                        final Class<? extends DataObject> childCls = (Class<? extends DataObject>)
                                factory().getRuntimeContext().getClassForSchema(childSchema);
                        return streamChild(childCls);
                    }

                    throw new UnsupportedOperationException("Unsupported child type " + childSchema.getClass());
                }
            });

    private final LoadingCache<SchemaPath, RpcInputCodec<?>> rpcDataByPath = CacheBuilder.newBuilder().build(
        new CacheLoader<SchemaPath, RpcInputCodec<?>>() {
            @Override
            public RpcInputCodec<?> load(final SchemaPath key) {
                final ContainerSchemaNode schema = SchemaContextUtil.getRpcDataSchema(getSchema(), key);
                @SuppressWarnings("unchecked")
                final Class<? extends DataContainer> cls = (Class<? extends DataContainer>)
                        factory().getRuntimeContext().getClassForSchema(schema);
                return getRpc(cls);
            }
        });

    private final LoadingCache<SchemaPath, NotificationCodecContext<?>> notificationsByPath = CacheBuilder.newBuilder()
            .build(new CacheLoader<SchemaPath, NotificationCodecContext<?>>() {
                @Override
                public NotificationCodecContext<?> load(final SchemaPath key) {
                    final NotificationDefinition schema = SchemaContextUtil.getNotificationSchema(getSchema(), key);
                    @SuppressWarnings("unchecked")
                    final Class<? extends Notification> clz = (Class<? extends Notification>)
                            factory().getRuntimeContext().getClassForSchema(schema);
                    return getNotification(clz);
                }
            });

    private SchemaRootCodecContext(final DataContainerCodecPrototype<SchemaContext> dataPrototype) {
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
        final DataContainerCodecPrototype<SchemaContext> prototype = DataContainerCodecPrototype.rootPrototype(factory);
        return new SchemaRootCodecContext<>(prototype);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <C extends DataObject> DataContainerCodecContext<C, ?> streamChild(final Class<C> childClass) {
        /* FIXME: This is still not solved for RPCs
         * TODO: Probably performance wise RPC, Data and Notification loading cache
         *       should be merge for performance resons. Needs microbenchmark to
         *       determine which is faster (keeping them separate or in same cache).
         */
        if (Notification.class.isAssignableFrom(childClass)) {
            return (DataContainerCodecContext<C, ?>) getNotification((Class<? extends Notification>)childClass);
        }
        return (DataContainerCodecContext<C, ?>) getOrRethrow(childrenByClass,childClass);
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
    public D deserialize(final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException("Could not create Binding data representation for root");
    }

    NotificationCodecContext<?> getNotification(final Class<? extends Notification> notification) {
        return getOrRethrow(notificationsByClass, notification);
    }

    NotificationCodecContext<?> getNotification(final SchemaPath notification) {
        return getOrRethrow(notificationsByPath, notification);
    }

    ContainerNodeCodecContext<?> getRpc(final Class<? extends DataContainer> rpcInputOrOutput) {
        return getOrRethrow(rpcDataByClass, rpcInputOrOutput);
    }

    RpcInputCodec<?> getRpc(final SchemaPath notification) {
        return getOrRethrow(rpcDataByPath, notification);
    }

    DataContainerCodecContext<?,?> createDataTreeChildContext(final Class<?> key) {
        final QName qname = BindingReflections.findQName(key);
        final DataSchemaNode childSchema = childNonNull(getSchema().getDataChildByName(qname), key,
            "%s is not top-level item.", key);
        return DataContainerCodecPrototype.from(key, childSchema, factory()).get();
    }

    ContainerNodeCodecContext<?> createRpcDataContext(final Class<?> key) {
        Preconditions.checkArgument(DataContainer.class.isAssignableFrom(key));
        final QName qname = BindingReflections.findQName(key);
        final QNameModule qnameModule = qname.getModule();
        final Module module = getSchema().findModule(qnameModule)
                .orElseThrow(() -> new IllegalArgumentException("Failed to find module for " + qnameModule));
        final String className = BindingMapping.getClassName(qname);

        RpcDefinition rpc = null;
        for (final RpcDefinition potential : module.getRpcs()) {
            final QName potentialQName = potential.getQName();
            /*
             * Check if rpc and class represents data from same module and then
             * checks if rpc local name produces same class name as class name
             * appended with Input/Output based on QName associated with bidning
             * class.
             *
             * FIXME: Rework this to have more precise logic regarding Binding
             * Specification.
             */
            if (key.getSimpleName().equals(BindingMapping.getClassName(potentialQName) + className)) {
                rpc = potential;
                break;
            }
        }
        Preconditions.checkArgument(rpc != null, "Supplied class %s is not valid RPC class.", key);
        final ContainerSchemaNode schema = SchemaNodeUtils.getRpcDataSchema(rpc, qname);
        Preconditions.checkArgument(schema != null, "Schema for %s does not define input / output.", rpc.getQName());
        return (ContainerNodeCodecContext<?>) DataContainerCodecPrototype.from(key, schema, factory()).get();
    }

    NotificationCodecContext<?> createNotificationDataContext(final Class<?> notificationType) {
        Preconditions.checkArgument(Notification.class.isAssignableFrom(notificationType));
        Preconditions.checkArgument(notificationType.isInterface(), "Supplied class must be interface.");
        final QName qname = BindingReflections.findQName(notificationType);
        /**
         *  FIXME: After Lithium cleanup of yang-model-api, use direct call on schema context
         *  to retrieve notification via index.
         */
        final NotificationDefinition schema = SchemaContextUtil.getNotificationSchema(getSchema(),
                SchemaPath.create(true, qname));
        Preconditions.checkArgument(schema != null, "Supplied %s is not valid notification", notificationType);

        return new NotificationCodecContext<>(notificationType, schema, factory());
    }

    ChoiceNodeCodecContext<?> createChoiceDataContext(final Class<? extends DataObject> caseType) {
        final Class<?> choiceClass = findCaseChoice(caseType);
        Preconditions.checkArgument(choiceClass != null, "Class %s is not a valid case representation", caseType);
        final DataSchemaNode schema = factory().getRuntimeContext().getSchemaDefinition(choiceClass);
        Preconditions.checkArgument(schema instanceof ChoiceSchemaNode, "Class %s does not refer to a choice",
            caseType);

        final DataContainerCodecContext<?, ChoiceSchemaNode> choice = DataContainerCodecPrototype.from(choiceClass,
            (ChoiceSchemaNode)schema, factory()).get();
        Verify.verify(choice instanceof ChoiceNodeCodecContext);
        return (ChoiceNodeCodecContext<?>) choice;
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException("Unable to deserialize root");
    }

    @Override
    public InstanceIdentifier.PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final InstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }

    @Override
    public DataContainerCodecContext<?, ?> bindingPathArgumentChild(final InstanceIdentifier.PathArgument arg,
            final List<PathArgument> builder) {
        final java.util.Optional<? extends Class<? extends DataObject>> caseType = arg.getCaseType();
        if (caseType.isPresent()) {
            final Class<? extends DataObject> type = caseType.get();
            final ChoiceNodeCodecContext<?> choice = choicesByClass.getUnchecked(type);
            choice.addYangPathArgument(arg, builder);
            final DataContainerCodecContext<?, ?> caze = choice.streamChild(type);
            caze.addYangPathArgument(arg, builder);
            return caze.bindingPathArgumentChild(arg, builder);
        }

        return super.bindingPathArgumentChild(arg, builder);
    }

    private static Class<?> findCaseChoice(final Class<? extends DataObject> caseClass) {
        for (Type type : caseClass.getGenericInterfaces()) {
            if (type instanceof Class) {
                final Class<?> typeClass = (Class<?>) type;
                if (ChoiceIn.class.isAssignableFrom(typeClass)) {
                    return typeClass.asSubclass(ChoiceIn.class);
                }
            }
        }

        return null;
    }

    private static <K,V> V getOrRethrow(final LoadingCache<K, V> cache, final K key) {
        try {
            return cache.getUnchecked(key);
        } catch (final UncheckedExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause != null) {
                Throwables.throwIfUnchecked(cause);
            }
            throw e;
        }
    }
}