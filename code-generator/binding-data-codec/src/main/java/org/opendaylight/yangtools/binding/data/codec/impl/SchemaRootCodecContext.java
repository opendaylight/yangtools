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
import javax.annotation.Nullable;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

class SchemaRootCodecContext extends DataContainerCodecContext<SchemaContext> {

    private final LoadingCache<Class<?>, DataContainerCodecContext<?>> dataChildren = CacheBuilder.newBuilder().build(
            new CacheLoader<Class<?>, DataContainerCodecContext<?>>() {
                @Override
                public DataContainerCodecContext<?> load(final Class<?> key) {
                    return createDataTreeChildContext(key);
                }

            });

    private final LoadingCache<Class<?>, DataContainerCodecContext<?>> rpcs = CacheBuilder.newBuilder().build(
            new CacheLoader<Class<?>, DataContainerCodecContext<?>>() {
                @Override
                public DataContainerCodecContext<?> load(final Class<?> key) {
                    return createRpcDataContext(key);
                }
            });

    private final LoadingCache<Class<?>, NotificationCodecContext> notifications = CacheBuilder.newBuilder().build(
            new CacheLoader<Class<?>, NotificationCodecContext>() {
                @Override
                public NotificationCodecContext load(final Class<?> key) {
                    return createNotificationDataContext(key);
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
        return dataChildren.getUnchecked(childClass);
    }

    @Override
    protected Optional<DataContainerCodecContext<?>> getPossibleStreamChild(final Class<?> childClass) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    protected YangInstanceIdentifier.PathArgument getDomPathArgument() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected NodeCodecContext getYangIdentifierChild(final YangInstanceIdentifier.PathArgument arg) {
        // FIXME: Optimize this
        final QName childQName = arg.getNodeType();
        final DataSchemaNode childSchema = schema().getDataChildByName(childQName);
        Preconditions.checkArgument(childSchema != null, "Argument %s is not valid child of %s", arg, schema());
        if (childSchema instanceof DataNodeContainer || childSchema instanceof ChoiceNode) {
            final Class<?> childCls = factory().getRuntimeContext().getClassForSchema(childSchema);
            final DataContainerCodecContext<?> childNode = getStreamChild(childCls);
            return childNode;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected Object dataFromNormalizedNode(final NormalizedNode<?, ?> normalizedNode) {
        throw new UnsupportedOperationException("Could not create Binding data representation for root");
    }

    NodeCodecContext getRpc(final Class<? extends DataContainer> rpcInputOrOutput) {
        return rpcs.getUnchecked(rpcInputOrOutput);
    }

    NotificationCodecContext getNotification(final Class<? extends Notification> notification) {
        return notifications.getUnchecked(notification);
    }

    NotificationCodecContext getNotification(final QName notification) {
        final NotificationDefinition schema = getNotificationSchema(schema(), notification);
        final Object clz = factory().getRuntimeContext().getClassForSchema(schema);
        return null;
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
        final ContainerSchemaNode schema = getRpcDataSchema(rpc, qname);
        Preconditions.checkArgument(schema != null, "Schema for %s does not define input / output.", rpc.getQName());
        return DataContainerCodecPrototype.from(key, schema, factory()).get();
    }

    protected NotificationCodecContext createNotificationDataContext(final Class<?> key) {
        Preconditions.checkArgument(Notification.class.isAssignableFrom(key));
        Preconditions.checkArgument(key.isInterface(), "Supplied class must be interface.");
        final QName qname = BindingReflections.findQName(key);

        final NotificationDefinition schema = getNotificationSchema(schema(),qname);
        Preconditions.checkArgument(schema != null, "Supplied %s is not valid notification", key);

        return new NotificationCodecContext(key,schema,factory());
    }

    // FIXME: This should be moved to yang-model-util probably.
    private static ContainerSchemaNode getRpcDataSchema(final RpcDefinition rpc, final QName qname) {
        switch (qname.getLocalName()) {
        case "input":
            return rpc.getInput();
        case "output":
            return rpc.getOutput();
        default:
            throw new IllegalArgumentException("Supplied qname " + qname + " does not represent rpc input or output.");
        }
    }

    private @Nullable NotificationDefinition getNotificationSchema(final SchemaContext schema, final QName qname) {
        // FIXME: Performance: Use indexed search (eg. by Class or
        // by QName) when available.
        for (final NotificationDefinition potential : schema().getNotifications()) {
            if (qname.equals(potential.getQName())) {
               return potential;
            }
        }
        return null;
    }
}