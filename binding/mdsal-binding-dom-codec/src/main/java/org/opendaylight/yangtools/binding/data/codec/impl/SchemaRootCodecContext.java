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
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
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
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
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

    private final LoadingCache<QName, DataContainerCodecContext<?,?>> childrenByQName = CacheBuilder.newBuilder().build(
            new CacheLoader<QName, DataContainerCodecContext<?,?>>() {
                @SuppressWarnings("unchecked")
                @Override
                public DataContainerCodecContext<?,?> load(final QName qname) {
                    final DataSchemaNode childSchema = getSchema().getDataChildByName(qname);
                    childNonNull(childSchema, qname,"Argument %s is not valid child of %s", qname,getSchema());
                    if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceSchemaNode) {
                        @SuppressWarnings("rawtypes")
                        final Class childCls = factory().getRuntimeContext().getClassForSchema(childSchema);
                        return streamChild(childCls);
                    }

                    throw new UnsupportedOperationException("Unsupported child type " + childSchema.getClass());
                }
            });

    private final LoadingCache<SchemaPath, RpcInputCodec<?>> rpcDataByPath = CacheBuilder.newBuilder().build(
        new CacheLoader<SchemaPath, RpcInputCodec<?>>() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public RpcInputCodec load(final SchemaPath key) {
                final ContainerSchemaNode schema = SchemaContextUtil.getRpcDataSchema(getSchema(), key);
                if (schema instanceof EffectiveStatement &&
                        ((EffectiveStatement) schema).getDeclared().getStatementSource() != StatementSource.DECLARATION) {
                    // This is an implicitly-defined input or output statement. We do not have a corresponding
                    // data representation, so we hard-wire it to null.
                    return UnmappedRpcInputCodec.getInstance();
                }

                final Class cls = factory().getRuntimeContext().getClassForSchema(schema);
                return getRpc(cls);
            }
        });

    private final LoadingCache<SchemaPath, NotificationCodecContext<?>> notificationsByPath = CacheBuilder.newBuilder()
            .build(new CacheLoader<SchemaPath, NotificationCodecContext<?>>() {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override
                public NotificationCodecContext load(final SchemaPath key) throws Exception {
                    final NotificationDefinition schema = SchemaContextUtil.getNotificationSchema(getSchema(), key);
                    final Class clz = factory().getRuntimeContext().getClassForSchema(schema);
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
     * @return
     */
    static SchemaRootCodecContext<?> create(final CodecContextFactory factory) {
        final DataContainerCodecPrototype<SchemaContext> prototype = DataContainerCodecPrototype.rootPrototype(factory);
        return new SchemaRootCodecContext<>(prototype);
    }


    @SuppressWarnings("unchecked")
    @Override
    public <DV extends DataObject> DataContainerCodecContext<DV, ?> streamChild(final Class<DV> childClass)
            throws IllegalArgumentException {
        /* FIXME: This is still not solved for RPCs
         * TODO: Probably performance wise RPC, Data and Notification loading cache
         *       should be merge for performance resons. Needs microbenchmark to
         *       determine which is faster (keeping them separate or in same cache).
         */
        if (Notification.class.isAssignableFrom(childClass)) {
            return (DataContainerCodecContext<DV, ?>) getNotification((Class<? extends Notification>)childClass);
        }
        return (DataContainerCodecContext<DV, ?>) getOrRethrow(childrenByClass,childClass);
    }

    @Override
    public <E extends DataObject> Optional<DataContainerCodecContext<E,?>> possibleStreamChild(final Class<E> childClass) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public DataContainerCodecContext<?,?> yangPathArgumentChild(final PathArgument arg) {
        return getOrRethrow(childrenByQName,arg.getNodeType());
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException(
                "Could not create Binding data representation for root");
    }


    ContainerNodeCodecContext<?> getRpc(final Class<? extends DataContainer> rpcInputOrOutput) {
        return getOrRethrow(rpcDataByClass, rpcInputOrOutput);
    }

    NotificationCodecContext<?> getNotification(final Class<? extends Notification> notification) {
        return getOrRethrow(notificationsByClass, notification);
    }

    NotificationCodecContext<?> getNotification(final SchemaPath notification) {
        return getOrRethrow(notificationsByPath, notification);
    }

    RpcInputCodec<?> getRpc(final SchemaPath notification) {
        return getOrRethrow(rpcDataByPath, notification);
    }

    private DataContainerCodecContext<?,?> createDataTreeChildContext(final Class<?> key) {
        final QName qname = BindingReflections.findQName(key);
        final DataSchemaNode childSchema = childNonNull(getSchema().getDataChildByName(qname),key,"%s is not top-level item.",key);
        return DataContainerCodecPrototype.from(key, childSchema, factory()).get();
    }

    private ContainerNodeCodecContext<?> createRpcDataContext(final Class<?> key) {
        Preconditions.checkArgument(DataContainer.class.isAssignableFrom(key));
        final QName qname = BindingReflections.findQName(key);
        final QNameModule module = qname.getModule();
        RpcDefinition rpc = null;
        for (final RpcDefinition potential : getSchema().getOperations()) {
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
            if (module.equals(potentialQName.getModule())
                    && key.getSimpleName().equals(
                            BindingMapping.getClassName(potentialQName) + BindingMapping.getClassName(qname))) {
                rpc = potential;
                break;
            }
        }
        Preconditions.checkArgument(rpc != null, "Supplied class %s is not valid RPC class.", key);
        final ContainerSchemaNode schema = SchemaNodeUtils.getRpcDataSchema(rpc, qname);
        Preconditions.checkArgument(schema != null, "Schema for %s does not define input / output.", rpc.getQName());
        return (ContainerNodeCodecContext<?>) DataContainerCodecPrototype.from(key, schema, factory()).get();
    }

    private NotificationCodecContext<?> createNotificationDataContext(final Class<?> notificationType) {
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