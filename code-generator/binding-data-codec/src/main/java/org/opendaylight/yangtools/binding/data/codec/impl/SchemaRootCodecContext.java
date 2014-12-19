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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;

final class SchemaRootCodecContext extends DataContainerCodecContext<SchemaContext> {

    private final LoadingCache<Class<?>, DataContainerCodecContext<?>> childrenByClass = CacheBuilder.newBuilder().build(
            new CacheLoader<Class<?>, DataContainerCodecContext<?>>() {
                @Override
                public DataContainerCodecContext<?> load(final Class<?> key) {
                    return createDataTreeChildContext(key);
                }

            });

    private final LoadingCache<Class<?>, DataContainerCodecContext<?>> rpcDataByClass = CacheBuilder.newBuilder().build(
            new CacheLoader<Class<?>, DataContainerCodecContext<?>>() {
                @Override
                public DataContainerCodecContext<?> load(final Class<?> key) {
                    return createRpcDataContext(key);
                }
            });

    private final LoadingCache<Class<?>, NotificationCodecContext> notificationsByClass = CacheBuilder.newBuilder().build(
    new CacheLoader<Class<?>, NotificationCodecContext>() {
        @Override
        public NotificationCodecContext load(final Class<?> key) {
            return createNotificationDataContext(key);
        }
    });

    private final LoadingCache<QName, DataContainerCodecContext<?>> childrenByQName = CacheBuilder.newBuilder().build(
        new CacheLoader<QName, DataContainerCodecContext<?>>() {
            @Override
            public DataContainerCodecContext<?> load(final QName qname) {
                final DataSchemaNode childSchema = schema().getDataChildByName(qname);
                Preconditions.checkArgument(childSchema != null, "Argument %s is not valid child of %s", qname, schema());

                if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceNode) {
                    final Class<?> childCls = factory().getRuntimeContext().getClassForSchema(childSchema);
                    return getStreamChild(childCls);
                } else {
                    throw new UnsupportedOperationException("Unsupported child type " + childSchema.getClass());
                }
            }
        });

    private final LoadingCache<SchemaPath, DataContainerCodecContext<?>> rpcDataByPath = CacheBuilder.newBuilder().build(
    new CacheLoader<SchemaPath, DataContainerCodecContext<?>>() {
        @Override
        public DataContainerCodecContext<?> load(final SchemaPath key) {
            // FIXME: Implement this
            throw new UnsupportedOperationException();
        }
    });

    private final LoadingCache<SchemaPath, NotificationCodecContext> notificationsByPath = CacheBuilder.newBuilder().build(
    new CacheLoader<SchemaPath, NotificationCodecContext>() {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public NotificationCodecContext load(final SchemaPath key) throws Exception {
            final NotificationDefinition schema = SchemaContextUtil.getNotificationSchema(schema(), key);
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
    static SchemaRootCodecContext create(final CodecContextFactory factory) {
        final DataContainerCodecPrototype<SchemaContext> prototype = DataContainerCodecPrototype.rootPrototype(factory);
        return new SchemaRootCodecContext(prototype);
    }

    @Override
    protected DataContainerCodecContext<?> getStreamChild(final Class<?> childClass) {
        return childrenByClass.getUnchecked(childClass);
    }

    @Override
    protected Optional<DataContainerCodecContext<?>> getPossibleStreamChild(final Class<?> childClass) {
        throw new UnsupportedOperationException("Not supported");
    }


    @Override
    protected NodeCodecContext getYangIdentifierChild(final PathArgument arg) {
        return childrenByQName.getUnchecked(arg.getNodeType());
    }

    @Override
    protected Object dataFromNormalizedNode(final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException("Could not create Binding data representation for root");
    }

    NodeCodecContext getRpc(final Class<? extends DataContainer> rpcInputOrOutput) {
        return rpcDataByClass.getUnchecked(rpcInputOrOutput);
    }

    NotificationCodecContext getNotification(final Class<? extends Notification> notification) {
        return notificationsByClass.getUnchecked(notification);
    }

    NotificationCodecContext getNotification(final SchemaPath notification) {
        return notificationsByPath.getUnchecked(notification);
    }

    DataContainerCodecContext<?> getRpc(final SchemaPath notification) {
        return rpcDataByPath.getUnchecked(notification);
    }

    private DataContainerCodecContext<?> createDataTreeChildContext(final Class<?> key) {
        final Class<Object> parent = ClassLoaderUtils.findFirstGenericArgument(key, ChildOf.class);
        Preconditions.checkArgument(DataRoot.class.isAssignableFrom(parent));
        final QName qname = BindingReflections.findQName(key);
        final DataSchemaNode childSchema = schema().getDataChildByName(qname);
        return DataContainerCodecPrototype.from(key, childSchema, factory()).get();
    }

    private DataContainerCodecContext<?> createRpcDataContext(final Class<?> key) {
        Preconditions.checkArgument(DataContainer.class.isAssignableFrom(key));
        final QName qname = BindingReflections.findQName(key);
        final QNameModule module = qname.getModule();
        RpcDefinition rpc = null;
        for (final RpcDefinition potential : schema().getOperations()) {
            final QName potentialQName = potential.getQName();
            /*
             *
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
        return DataContainerCodecPrototype.from(key, schema, factory()).get();
    }

    private NotificationCodecContext createNotificationDataContext(final Class<?> notificationType) {
        Preconditions.checkArgument(Notification.class.isAssignableFrom(notificationType));
        Preconditions.checkArgument(notificationType.isInterface(), "Supplied class must be interface.");
        final QName qname = BindingReflections.findQName(notificationType);
        // FIXME: Use different approach
        final NotificationDefinition schema = SchemaContextUtil.getNotificationSchema(schema(),SchemaPath.create(true, qname));
        Preconditions.checkArgument(schema != null, "Supplied %s is not valid notification", notificationType);

        return new NotificationCodecContext(notificationType,schema,factory());
    }


}