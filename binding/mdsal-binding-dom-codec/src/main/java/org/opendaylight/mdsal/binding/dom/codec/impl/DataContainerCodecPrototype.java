/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode.ChildAddressabilitySummary;
import org.opendaylight.mdsal.binding.dom.codec.impl.NodeCodecContext.CodecContextFactory;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeTypeContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DataContainerCodecPrototype<T extends RuntimeTypeContainer> implements NodeContextSupplier {
    private static final Logger LOG = LoggerFactory.getLogger(DataContainerCodecPrototype.class);

    private static final VarHandle INSTANCE;

    static {
        try {
            INSTANCE = MethodHandles.lookup().findVarHandle(DataContainerCodecPrototype.class,
                "instance", DataContainerCodecContext.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull T type;
    private final @NonNull QNameModule namespace;
    private final @NonNull CodecContextFactory factory;
    private final @NonNull Item<?> bindingArg;
    private final @NonNull PathArgument yangArg;
    private final @NonNull ChildAddressabilitySummary childAddressabilitySummary;

    // Accessed via INSTANCE
    @SuppressWarnings("unused")
    private volatile DataContainerCodecContext<?, T> instance;

    @SuppressWarnings("unchecked")
    private DataContainerCodecPrototype(final Class<?> cls, final PathArgument yangArg, final T type,
            final CodecContextFactory factory) {
        this(Item.of((Class<? extends DataObject>) cls), yangArg, type, factory);
    }

    private DataContainerCodecPrototype(final Item<?> bindingArg, final PathArgument yangArg, final T type,
            final CodecContextFactory factory) {
        this.bindingArg = requireNonNull(bindingArg);
        this.yangArg = requireNonNull(yangArg);
        this.type = requireNonNull(type);
        this.factory = requireNonNull(factory);

        if (yangArg instanceof AugmentationIdentifier augId) {
            final var childNames = augId.getPossibleChildNames();
            verify(!childNames.isEmpty(), "Unexpected empty identifier for %s", type);
            namespace = childNames.iterator().next().getModule();
        } else {
            namespace = yangArg.getNodeType().getModule();
        }

        childAddressabilitySummary = type instanceof RuntimeType
            ? computeChildAddressabilitySummary(((RuntimeType) type).statement())
                // BindingRuntimeTypes, does not matter
                : ChildAddressabilitySummary.MIXED;
    }

    private static @NonNull ChildAddressabilitySummary computeChildAddressabilitySummary(final Object nodeSchema) {
        // FIXME: rework this to work on EffectiveStatements
        if (nodeSchema instanceof DataNodeContainer contaner) {
            boolean haveAddressable = false;
            boolean haveUnaddressable = false;
            for (DataSchemaNode child : contaner.getChildNodes()) {
                if (child instanceof ContainerSchemaNode || child instanceof AugmentationSchemaNode) {
                    haveAddressable = true;
                } else if (child instanceof ListSchemaNode list) {
                    if (list.getKeyDefinition().isEmpty()) {
                        haveUnaddressable = true;
                    } else {
                        haveAddressable = true;
                    }
                } else if (child instanceof AnydataSchemaNode || child instanceof AnyxmlSchemaNode
                        || child instanceof TypedDataSchemaNode) {
                    haveUnaddressable = true;
                } else if (child instanceof ChoiceSchemaNode choice) {
                    switch (computeChildAddressabilitySummary(choice)) {
                        case ADDRESSABLE -> haveAddressable = true;
                        case UNADDRESSABLE -> haveUnaddressable = true;
                        case MIXED -> {
                            haveAddressable = true;
                            haveUnaddressable = true;
                        }
                        default -> throw new IllegalStateException("Unhandled accessibility summary for " + child);
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
        } else if (nodeSchema instanceof ChoiceSchemaNode choice) {
            return computeChildAddressabilitySummary(choice);
        }

        // No child nodes possible: return unaddressable
        return ChildAddressabilitySummary.UNADDRESSABLE;
    }

    private static @NonNull ChildAddressabilitySummary computeChildAddressabilitySummary(
            final ChoiceSchemaNode choice) {
        boolean haveAddressable = false;
        boolean haveUnaddressable = false;
        for (CaseSchemaNode child : choice.getCases()) {
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

    static DataContainerCodecPrototype<BindingRuntimeTypes> rootPrototype(final CodecContextFactory factory) {
        return new DataContainerCodecPrototype<>(DataRoot.class, NodeIdentifier.create(SchemaContext.NAME),
            factory.getRuntimeContext().getTypes(), factory);
    }

    static <T extends CompositeRuntimeType> DataContainerCodecPrototype<T> from(final Class<?> cls, final T type,
            final CodecContextFactory factory) {
        return new DataContainerCodecPrototype<>(cls, createIdentifier(type), type, factory);
    }

    static <T extends CompositeRuntimeType> DataContainerCodecPrototype<T> from(final Item<?> bindingArg, final T type,
            final CodecContextFactory factory) {
        return new DataContainerCodecPrototype<>(bindingArg, createIdentifier(type), type, factory);
    }

    static DataContainerCodecPrototype<AugmentRuntimeType> from(final Class<?> augClass,
            final AugmentationIdentifier arg, final AugmentRuntimeType schema, final CodecContextFactory factory) {
        return new DataContainerCodecPrototype<>(augClass, arg, schema, factory);
    }

    static DataContainerCodecPrototype<NotificationRuntimeType> from(final Class<?> augClass,
            final NotificationRuntimeType schema, final CodecContextFactory factory) {
        return new DataContainerCodecPrototype<>(augClass, NodeIdentifier.create(schema.statement().argument()), schema,
            factory);
    }

    private static @NonNull NodeIdentifier createIdentifier(final CompositeRuntimeType type) {
        final Object arg = type.statement().argument();
        verify(arg instanceof QName, "Unexpected type %s argument %s", type, arg);
        return NodeIdentifier.create((QName) arg);
    }

    @NonNull T getType() {
        return type;
    }

    @NonNull ChildAddressabilitySummary getChildAddressabilitySummary() {
        return childAddressabilitySummary;
    }

    @NonNull QNameModule getNamespace() {
        return namespace;
    }

    @NonNull CodecContextFactory getFactory() {
        return factory;
    }

    @NonNull Class<?> getBindingClass() {
        return bindingArg.getType();
    }

    @NonNull Item<?> getBindingArg() {
        return bindingArg;
    }

    @NonNull PathArgument getYangArg() {
        return yangArg;
    }

    @Override
    public DataContainerCodecContext<?, T> get() {
        final DataContainerCodecContext<?, T> existing = (DataContainerCodecContext<?, T>) INSTANCE.getAcquire(this);
        return existing != null ? existing : loadInstance();
    }

    private @NonNull DataContainerCodecContext<?, T> loadInstance() {
        final var tmp = createInstance();
        final var witness = (DataContainerCodecContext<?, T>) INSTANCE.compareAndExchangeRelease(this, null, tmp);
        return witness == null ? tmp : witness;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    // This method must allow concurrent loading, i.e. nothing in it may have effects outside of the loaded object
    private @NonNull DataContainerCodecContext<?, T> createInstance() {
        // FIXME: make protected abstract
        if (type instanceof ContainerLikeRuntimeType containerLike) {
            if (containerLike instanceof ContainerRuntimeType container
                && container.statement().findFirstEffectiveSubstatement(PresenceEffectiveStatement.class).isEmpty()) {
                return new NonPresenceContainerNodeCodecContext(this);
            }
            return new ContainerNodeCodecContext(this);
        } else if (type instanceof ListRuntimeType) {
            return Identifiable.class.isAssignableFrom(getBindingClass())
                    ? KeyedListNodeCodecContext.create((DataContainerCodecPrototype<ListRuntimeType>) this)
                            : new ListNodeCodecContext(this);
        } else if (type instanceof ChoiceRuntimeType) {
            return new ChoiceNodeCodecContext(this);
        } else if (type instanceof AugmentRuntimeType) {
            return new AugmentationNodeContext(this);
        } else if (type instanceof CaseRuntimeType) {
            return new CaseNodeCodecContext(this);
        }
        throw new IllegalArgumentException("Unsupported type " + getBindingClass() + " " + type);
    }
}
