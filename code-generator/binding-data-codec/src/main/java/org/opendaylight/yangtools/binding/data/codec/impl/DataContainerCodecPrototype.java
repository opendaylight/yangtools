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
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

class DataContainerCodecPrototype<T> implements NodeContextSupplier {

    private final T schema;
    private final QNameModule namespace;
    private final CodecContextFactory factory;
    private final Class<?> bindingClass;
    private final InstanceIdentifier.Item<?> bindingArg;
    private final YangInstanceIdentifier.PathArgument yangArg;
    private volatile DataContainerCodecContext<T> instance = null;

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

    private DataContainerCodecPrototype(final Class<?> cls, final YangInstanceIdentifier.PathArgument arg, final T nodeSchema,
            final CodecContextFactory factory,final DataContainerCodecContext<T> instance) {
        super();
        this.yangArg = arg;
        this.schema = nodeSchema;
        this.factory = factory;
        this.namespace = arg.getNodeType().getModule();
        this.bindingClass = cls;
        this.bindingArg = new InstanceIdentifier.Item(bindingClass);
        this.instance = instance;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <T extends DataSchemaNode> DataContainerCodecPrototype<T> from(final Class<?> cls, final T schema,
            final CodecContextFactory factory) {
        NodeIdentifier arg = new NodeIdentifier(schema.getQName());
        return new DataContainerCodecPrototype(cls, arg, schema, factory);
    }

    static DataContainerCodecPrototype<SchemaContext> rootPrototype(final CodecContextFactory factory) {
        SchemaContext schema = factory.getRuntimeContext().getSchemaContext();
        NodeIdentifier arg = new NodeIdentifier(schema.getQName());
        return new DataContainerCodecPrototype<SchemaContext>(DataRoot.class, arg, schema, factory);
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static DataContainerCodecPrototype<?> from(final Class<?> augClass, final AugmentationIdentifier arg,
            final AugmentationSchema schema, final CodecContextFactory factory) {
        return new DataContainerCodecPrototype(augClass, arg, schema, factory);
    }

    protected final T getSchema() {
        return schema;
    }

    protected final QNameModule getNamespace() {
        return namespace;
    }

    protected final CodecContextFactory getFactory() {
        return factory;
    }

    protected final Class<?> getBindingClass() {
        return bindingClass;
    }

    protected final InstanceIdentifier.Item<?> getBindingArg() {
        return bindingArg;
    }

    protected final YangInstanceIdentifier.PathArgument getYangArg() {
        return yangArg;
    }

    @Override
    public DataContainerCodecContext<T> get() {
        DataContainerCodecContext<T> tmp = instance;
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
    private DataContainerCodecContext createInstance() {
        // FIXME: make protected abstract
        if (schema instanceof ContainerSchemaNode) {
            return new ContainerNodeCodecContext((DataContainerCodecPrototype) this);
        } else if (schema instanceof ListSchemaNode) {
            if (Identifiable.class.isAssignableFrom(getBindingClass())) {
                return new KeyedListNodeCodecContext((DataContainerCodecPrototype) this);
            } else {
                return new ListNodeCodecContext((DataContainerCodecPrototype) this);
            }
        } else if (schema instanceof ChoiceNode) {
            return new ChoiceNodeCodecContext((DataContainerCodecPrototype) this);
        } else if (schema instanceof AugmentationSchema) {
            return new AugmentationNodeContext((DataContainerCodecPrototype) this);
        } else if (schema instanceof ChoiceCaseNode) {
            return new CaseNodeCodecContext((DataContainerCodecPrototype) this);
        }
        throw new IllegalArgumentException("Unsupported type " + bindingClass + " " + schema);
    }

    boolean isChoice() {
        return schema instanceof ChoiceNode;
    }
}
