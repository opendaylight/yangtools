/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.Iterables;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.yangtools.binding.data.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class DataContainerCodecPrototype<T> implements NodeContextSupplier {

    private final T schema;
    private final QNameModule namespace;
    private final CodecContextFactory factory;
    private final Class<?> bindingClass;
    private final InstanceIdentifier.Item<?> bindingArg;
    private final YangInstanceIdentifier.PathArgument yangArg;
    private volatile DataContainerCodecContext<?,T> instance = null;

    @SuppressWarnings({"rawtypes", "unchecked"})
    private DataContainerCodecPrototype(final Class<?> cls, final YangInstanceIdentifier.PathArgument arg, final T nodeSchema,
            final CodecContextFactory factory) {
        super();
        this.bindingClass = cls;
        this.yangArg = arg;
        this.schema = nodeSchema;
        this.factory = factory;
        this.bindingArg = new InstanceIdentifier.Item(bindingClass);

        if (arg instanceof AugmentationIdentifier) {
            this.namespace = Iterables.getFirst(((AugmentationIdentifier) arg).getPossibleChildNames(), null).getModule();
        } else {
            this.namespace = arg.getNodeType().getModule();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <T extends DataSchemaNode> DataContainerCodecPrototype<T> from(final Class<?> cls, final T schema,
            final CodecContextFactory factory) {
        final NodeIdentifier arg = new NodeIdentifier(schema.getQName());
        return new DataContainerCodecPrototype(cls, arg, schema, factory);
    }

    static DataContainerCodecPrototype<SchemaContext> rootPrototype(final CodecContextFactory factory) {
        final SchemaContext schema = factory.getRuntimeContext().getSchemaContext();
        final NodeIdentifier arg = new NodeIdentifier(schema.getQName());
        return new DataContainerCodecPrototype<SchemaContext>(DataRoot.class, arg, schema, factory);
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    static DataContainerCodecPrototype<?> from(final Class<?> augClass, final AugmentationIdentifier arg,
            final AugmentationSchema schema, final CodecContextFactory factory) {
        return new DataContainerCodecPrototype(augClass, arg, schema, factory);
    }

    static DataContainerCodecPrototype<NotificationDefinition> from(final Class<?> augClass, final NotificationDefinition schema, final CodecContextFactory factory) {
        final PathArgument arg = new NodeIdentifier(schema.getQName());
        return new DataContainerCodecPrototype<NotificationDefinition>(augClass,arg, schema, factory);
    }

    protected T getSchema() {
        return schema;
    }

    protected QNameModule getNamespace() {
        return namespace;
    }

    protected CodecContextFactory getFactory() {
        return factory;
    }

    protected Class<?> getBindingClass() {
        return bindingClass;
    }

    protected InstanceIdentifier.Item<?> getBindingArg() {
        return bindingArg;
    }

    protected YangInstanceIdentifier.PathArgument getYangArg() {
        return yangArg;
    }

    @Override
    public DataContainerCodecContext<?,T> get() {
        DataContainerCodecContext<?,T> tmp = instance;
        if (tmp == null) {
            synchronized (this) {
                tmp = instance;
                if (tmp == null) {
                    tmp = createInstance();
                    instance = tmp;
                }
            }
        }

        return tmp;
    }

    @GuardedBy("this")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private DataContainerCodecContext<?,T> createInstance() {
        // FIXME: make protected abstract
        if (schema instanceof ContainerSchemaNode) {
            return new ContainerNodeCodecContext(this);
        } else if (schema instanceof ListSchemaNode) {
            if (Identifiable.class.isAssignableFrom(getBindingClass())) {
                return new KeyedListNodeCodecContext(this);
            } else {
                return new ListNodeCodecContext(this);
            }
        } else if (schema instanceof ChoiceSchemaNode) {
            return new ChoiceNodeCodecContext(this);
        } else if (schema instanceof AugmentationSchema) {
            return new AugmentationNodeContext(this);
        } else if (schema instanceof ChoiceCaseNode) {
            return new CaseNodeCodecContext(this);
        }
        throw new IllegalArgumentException("Unsupported type " + bindingClass + " " + schema);
    }

    boolean isChoice() {
        return schema instanceof ChoiceSchemaNode;
    }
}
