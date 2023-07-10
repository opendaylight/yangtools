/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

/**
 * Abstract base for {@link DataObjectCodecContext} and {@link AugmentationCodecContext}. They share most of their
 * mechanics, but notably:
 * <ol>
 *   <li>DataObjectCodecContext has an exact DistinctNodeContainer and YangInstanceIdentifier mapping and can be the
 *       target of augmentations (i.e. can implement Augmentable contract)</li>
 *   <li>AugmentationNodeContext has neither of those traits and really is just a filter of its parent
 *       DistinctNodeContainer</li>
 * </ol>
 *
 * <p>
 * Unfortunately {@code Augmentation} is a also a {@link DataObject}, so things get a bit messy.
 *
 * <p>
 * While this class is public, it not part of API surface and is an implementation detail. The only reason for it being
 * public is that it needs to be accessible by code generated at runtime.
 */
public abstract sealed class AbstractDataObjectCodecContext<D extends DataObject, T extends CompositeRuntimeType>
        extends CommonDataObjectCodecContext<D, T>
        permits AugmentationCodecContext, DataObjectCodecContext {
    private final ImmutableMap<Class<?>, CommonDataObjectCodecPrototype<?>> byBindingArgClass;
    private final ImmutableMap<Class<?>, CommonDataObjectCodecPrototype<?>> byStreamClass;
    private final ImmutableMap<NodeIdentifier, CodecContextSupplier> byYang;
    private final ImmutableMap<String, ValueNodeCodecContext> leafChild;

    AbstractDataObjectCodecContext(final CommonDataObjectCodecPrototype<T> prototype,
            final DataContainerAnalysis<T> analysis) {
        super(prototype);
        byBindingArgClass = analysis.byBindingArgClass;
        byStreamClass = analysis.byStreamClass;
        byYang = analysis.byYang;
        leafChild = analysis.leafNodes;
    }

    @Override
    public final WithStatus getSchema() {
        // FIXME: Bad cast, we should be returning an EffectiveStatement perhaps?
        return (WithStatus) type().statement();
    }

    @Override
    CommonDataObjectCodecPrototype<?> streamChildPrototype(final Class<?> childClass) {
        return byStreamClass.get(childClass);
    }

    @Override
    public final CommonDataObjectCodecContext<?, ?> bindingPathArgumentChild(final InstanceIdentifier.PathArgument arg,
            final List<PathArgument> builder) {
        final var argType = arg.getType();
        final var context = childNonNull(pathChildPrototype(argType), argType,
            "Class %s is not valid child of %s", argType, getBindingClass())
            .get();
        if (context instanceof ChoiceCodecContext<?> choice) {
            choice.addYangPathArgument(arg, builder);

            final var caseType = arg.getCaseType();
            final var type = arg.getType();
            final DataContainerCodecContext<?, ?> caze;
            if (caseType.isPresent()) {
                // Non-ambiguous addressing this should not pose any problems
                caze = choice.getStreamChild(caseType.orElseThrow());
            } else {
                caze = choice.getCaseByChildClass(type);
            }

            caze.addYangPathArgument(arg, builder);
            return caze.bindingPathArgumentChild(arg, builder);
        }
        context.addYangPathArgument(arg, builder);
        return context;
    }

    @Nullable CommonDataObjectCodecPrototype<?> pathChildPrototype(final @NonNull Class<? extends DataObject> argType) {
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
