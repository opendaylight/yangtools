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
import org.opendaylight.mdsal.binding.dom.codec.api.CommonDataObjectCodecTreeNode.ChildAddressabilitySummary;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeTypeContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract sealed class DataContainerCodecPrototype<T extends RuntimeTypeContainer> implements CodecContextSupplier
        permits AugmentationCodecPrototype, DataObjectCodecPrototype {
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
    private final @NonNull ChildAddressabilitySummary childAddressabilitySummary;

    // multiple paths represent augmentation wrapper
    // FIXME: this means it is either this or 'childArgs'

    // Accessed via INSTANCE
    @SuppressWarnings("unused")
    private volatile DataContainerCodecContext<?, T> instance;

    DataContainerCodecPrototype(final Item<?> bindingArg, final QNameModule namespace, final T type,
            final CodecContextFactory factory) {
        this.bindingArg = requireNonNull(bindingArg);
        this.namespace = requireNonNull(namespace);
        this.type = requireNonNull(type);
        this.factory = requireNonNull(factory);

        childAddressabilitySummary = type instanceof RuntimeType runtimeType
            ? computeChildAddressabilitySummary(runtimeType.statement())
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

    static <T extends CompositeRuntimeType> DataContainerCodecPrototype<T> from(final Class<?> cls, final T type,
            final CodecContextFactory factory) {
        return new DataObjectCodecPrototype<>(cls, createIdentifier(type), type, factory);
    }

    static <T extends CompositeRuntimeType> DataContainerCodecPrototype<T> from(final Item<?> bindingArg, final T type,
            final CodecContextFactory factory) {
        return new DataObjectCodecPrototype<>(bindingArg, createIdentifier(type), type, factory);
    }

    private static @NonNull NodeIdentifier createIdentifier(final CompositeRuntimeType type) {
        final Object arg = type.statement().argument();
        verify(arg instanceof QName, "Unexpected type %s argument %s", type, arg);
        return NodeIdentifier.create((QName) arg);
    }

    final @NonNull T getType() {
        return type;
    }

    final @NonNull ChildAddressabilitySummary getChildAddressabilitySummary() {
        return childAddressabilitySummary;
    }

    final @NonNull QNameModule getNamespace() {
        return namespace;
    }

    final @NonNull CodecContextFactory getFactory() {
        return factory;
    }

    final @NonNull Class<?> getBindingClass() {
        return bindingArg.getType();
    }

    final @NonNull Item<?> getBindingArg() {
        return bindingArg;
    }

    abstract @NonNull NodeIdentifier getYangArg();

    @Override
    public final DataContainerCodecContext<?, T> get() {
        final var existing = (DataContainerCodecContext<?, T>) INSTANCE.getAcquire(this);
        return existing != null ? existing : loadInstance();
    }

    @SuppressWarnings("unchecked")
    final <R extends CompositeRuntimeType> DataObjectCodecContext<?, R> getDataObject() {
        final var context = get();
        verify(context instanceof DataObjectCodecContext, "Unexpected instance %s", context);
        return (DataObjectCodecContext<?, R>) context;
    }

    private @NonNull DataContainerCodecContext<?, T> loadInstance() {
        final var tmp = createInstance();
        final var witness = (DataContainerCodecContext<?, T>) INSTANCE.compareAndExchangeRelease(this, null, tmp);
        return witness == null ? tmp : witness;
    }

    // This method must allow concurrent loading, i.e. nothing in it may have effects outside of the loaded object
    abstract @NonNull DataContainerCodecContext<?, T> createInstance();
}
