/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.collect.Iterables;
import javax.annotation.concurrent.GuardedBy;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingCodecTreeNode.ChildAddressabilitySummary;
import org.opendaylight.mdsal.binding.dom.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DataContainerCodecPrototype<T extends WithStatus> implements NodeContextSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(DataContainerCodecPrototype.class);

    private final T schema;
    private final QNameModule namespace;
    private final CodecContextFactory factory;
    private final Class<?> bindingClass;
    private final Item<?> bindingArg;
    private final PathArgument yangArg;
    private final ChildAddressabilitySummary childAddressabilitySummary;

    private volatile DataContainerCodecContext<?, T> instance = null;

    @SuppressWarnings("unchecked")
    private DataContainerCodecPrototype(final Class<?> cls, final PathArgument arg, final T nodeSchema,
            final CodecContextFactory factory) {
        this.bindingClass = cls;
        this.yangArg = arg;
        this.schema = nodeSchema;
        this.factory = factory;
        this.bindingArg = Item.of((Class<? extends DataObject>) bindingClass);

        if (arg instanceof AugmentationIdentifier) {
            this.namespace = Iterables.getFirst(((AugmentationIdentifier) arg).getPossibleChildNames(), null)
                    .getModule();
        } else {
            this.namespace = arg.getNodeType().getModule();
        }

        this.childAddressabilitySummary = computeChildAddressabilitySummary(nodeSchema);
    }

    private static ChildAddressabilitySummary computeChildAddressabilitySummary(final WithStatus nodeSchema) {
        if (nodeSchema instanceof DataNodeContainer) {
            boolean haveAddressable = false;
            boolean haveUnaddressable = false;
            for (DataSchemaNode child : ((DataNodeContainer) nodeSchema).getChildNodes()) {
                if (child instanceof ContainerSchemaNode || child instanceof AugmentationSchemaNode) {
                    haveAddressable = true;
                } else if (child instanceof ListSchemaNode) {
                    if (((ListSchemaNode) child).getKeyDefinition().isEmpty()) {
                        haveUnaddressable = true;
                    } else {
                        haveAddressable = true;
                    }
                } else if (child instanceof AnyDataSchemaNode || child instanceof AnyXmlSchemaNode
                        || child instanceof TypedDataSchemaNode) {
                    haveUnaddressable = true;
                } else if (child instanceof ChoiceSchemaNode) {
                    switch (computeChildAddressabilitySummary(child)) {
                        case ADDRESSABLE:
                            haveAddressable = true;
                            break;
                        case MIXED:
                            haveAddressable = true;
                            haveUnaddressable = true;
                            break;
                        case UNADDRESSABLE:
                            haveUnaddressable = true;
                            break;
                        default:
                            throw new IllegalStateException("Unhandled accessibility summary for " + child);
                    }
                } else {
                    LOG.warn("Unhandled child node {}", child);
                }
            }

            if (!haveAddressable) {
                // Empty or all are unaddressable
                return ChildAddressabilitySummary.UNADDRESSABLE;
            }

            return haveUnaddressable ? ChildAddressabilitySummary.MIXED : ChildAddressabilitySummary.ADDRESSABLE;
        } else if (nodeSchema instanceof ChoiceSchemaNode) {
            boolean haveAddressable = false;
            boolean haveUnaddressable = false;
            for (CaseSchemaNode child : ((ChoiceSchemaNode) nodeSchema).getCases().values()) {
                switch (computeChildAddressabilitySummary(child)) {
                    case ADDRESSABLE:
                        haveAddressable = true;
                        break;
                    case UNADDRESSABLE:
                        haveUnaddressable = true;
                        break;
                    case MIXED:
                        // A child is mixed, which means we are mixed, too
                        return ChildAddressabilitySummary.MIXED;
                    default:
                        throw new IllegalStateException("Unhandled accessibility summary for " + child);
                }
            }

            if (!haveAddressable) {
                // Empty or all are unaddressable
                return ChildAddressabilitySummary.UNADDRESSABLE;
            }

            return haveUnaddressable ? ChildAddressabilitySummary.MIXED : ChildAddressabilitySummary.ADDRESSABLE;
        }

        // No child nodes possible: return unaddressable
        return ChildAddressabilitySummary.UNADDRESSABLE;
    }

    static DataContainerCodecPrototype<SchemaContext> rootPrototype(final CodecContextFactory factory) {
        final SchemaContext schema = factory.getRuntimeContext().getSchemaContext();
        final NodeIdentifier arg = NodeIdentifier.create(schema.getQName());
        return new DataContainerCodecPrototype<>(DataRoot.class, arg, schema, factory);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <T extends DataSchemaNode> DataContainerCodecPrototype<T> from(final Class<?> cls, final T schema,
            final CodecContextFactory factory) {
        return new DataContainerCodecPrototype(cls, NodeIdentifier.create(schema.getQName()), schema, factory);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static DataContainerCodecPrototype<?> from(final Class<?> augClass, final AugmentationIdentifier arg,
            final AugmentationSchemaNode schema, final CodecContextFactory factory) {
        return new DataContainerCodecPrototype(augClass, arg, schema, factory);
    }

    static DataContainerCodecPrototype<NotificationDefinition> from(final Class<?> augClass,
            final NotificationDefinition schema, final CodecContextFactory factory) {
        final PathArgument arg = NodeIdentifier.create(schema.getQName());
        return new DataContainerCodecPrototype<>(augClass,arg, schema, factory);
    }

    protected T getSchema() {
        return schema;
    }

    ChildAddressabilitySummary getChildAddressabilitySummary() {
        return childAddressabilitySummary;
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

    protected Item<?> getBindingArg() {
        return bindingArg;
    }

    protected PathArgument getYangArg() {
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
            return Identifiable.class.isAssignableFrom(getBindingClass()) ? new KeyedListNodeCodecContext(this)
                    : new ListNodeCodecContext(this);
        } else if (schema instanceof ChoiceSchemaNode) {
            return new ChoiceNodeCodecContext(this);
        } else if (schema instanceof AugmentationSchemaNode) {
            return new AugmentationNodeContext(this);
        } else if (schema instanceof CaseSchemaNode) {
            return new CaseNodeCodecContext(this);
        }
        throw new IllegalArgumentException("Unsupported type " + bindingClass + " " + schema);
    }

    boolean isChoice() {
        return schema instanceof ChoiceSchemaNode;
    }
}
