/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.data.codec.api.CommonDataObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.IncorrectNestingException;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;


/**
 * Base implementation of {@link CommonDataObjectCodecTreeNode}, shared between {@link DataObjectCodecContext} and
 * {@link AugmentationCodecContext}. They share most of their mechanics, but notably:
 * <ol>
 *   <li>DataObjectCodecContext has an exact DistinctNodeContainer and YangInstanceIdentifier mapping and can be the
 *       target of augmentations (i.e. can implement Augmentable contract)</li>
 *   <li>AugmentationNodeContext has neither of those traits and really is just a filter of its parent
 *       DistinctNodeContainer</li>
 * </ol>
 *
 * <p>Unfortunately {@code Augmentation} is a also a {@link DataObject}, so things get a bit messy.
 *
 * <p>While this class is public, it not part of API surface and is an implementation detail. The only reason for it
 * being public is that it needs to be accessible by code generated at runtime.
 */
public abstract sealed class CommonDataObjectCodecContext<D extends DataObject, T extends CompositeRuntimeType>
        extends DataContainerCodecContext<D, T, CommonDataObjectCodecPrototype<T>>
        implements CommonDataObjectCodecTreeNode<D>
        permits AugmentationCodecContext, DataObjectCodecContext {
    private final ImmutableMap<Class<?>, DataContainerPrototype<?, ?>> byBindingArgClass;
    private final ImmutableMap<Class<?>, DataContainerPrototype<?, ?>> byStreamClass;
    private final ImmutableMap<NodeIdentifier, CodecContextSupplier> byYang;
    private final ImmutableMap<String, ValueNodeCodecContext> leafChild;

    CommonDataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype,
            final DataContainerAnalysis<T> analysis) {
        super(prototype);
        byBindingArgClass = analysis.byBindingArgClass;
        byStreamClass = analysis.byStreamClass;
        byYang = analysis.byYang;
        leafChild = analysis.leafNodes;
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public final WithStatus getSchema() {
        // FIXME: Bad cast, we should be returning an EffectiveStatement perhaps?
        return (WithStatus) prototype().runtimeType().statement();
    }

    @Override
    public final CommonDataObjectCodecContext<?, ?> bindingPathArgumentChild(final DataObjectStep<?> step,
            final List<PathArgument> builder) {
        final var type = step.type();
        final var context = childNonNull(pathChildPrototype(type), type,
            "Class %s is not valid child of %s", type, getBindingClass())
            .getCodecContext();
        context.addYangPathArgument(step, builder);
        return switch (context) {
            case ChoiceCodecContext<?> choice -> choice.bindingPathArgumentChild(step, builder);
            case CommonDataObjectCodecContext<?, ?> dataObject -> dataObject;
            default -> throw new IllegalStateException("Unhandled context " + context);
        };
    }

    /**
     * Returns deserialized Binding Path Argument from YANG instance identifier.
     */
    protected DataObjectStep<?> getBindingPathArgument(final PathArgument domArg) {
        return bindingArg();
    }

    protected final DataObjectStep<?> bindingArg() {
        return prototype().getBindingArg();
    }


    @Override
    DataContainerPrototype<?, ?> streamChildPrototype(final Class<?> childClass) {
        return byStreamClass.get(childClass);
    }

    @Nullable DataContainerPrototype<?, ?> pathChildPrototype(final @NonNull Class<? extends DataObject> argType) {
        return byBindingArgClass.get(argType);
    }

    @Override
    CodecContextSupplier yangChildSupplier(final NodeIdentifier arg) {
        return byYang.get(arg);
    }

    final ValueNodeCodecContext getLeafChild(final String name) {
        final ValueNodeCodecContext value = leafChild.get(name);
        if (value == null) {
            throw new IncorrectNestingException("Leaf %s is not valid for %s", name, getBindingClass());
        }
        return value;
    }

    final @NonNull ImmutableSet<NodeIdentifier> byYangKeySet() {
        return byYang.keySet();
    }

    abstract @NonNull Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(
        DataContainerNode data);
}
