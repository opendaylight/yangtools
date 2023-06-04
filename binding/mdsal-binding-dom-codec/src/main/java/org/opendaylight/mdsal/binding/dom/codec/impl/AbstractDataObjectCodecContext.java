/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DistinctNodeContainer;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

/**
 * Abstract base for {@link DataObjectCodecContext} and {@link AugmentationNodeContext}. They share most of their
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
public abstract class AbstractDataObjectCodecContext<D extends DataObject, T extends CompositeRuntimeType>
        extends DataContainerCodecContext<D, T> {
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byBindingArgClass;
    private final ImmutableMap<Class<?>, DataContainerCodecPrototype<?>> byStreamClass;
    private final ImmutableMap<NodeIdentifier, NodeContextSupplier> byYang;
    private final ImmutableMap<String, ValueNodeCodecContext> leafChild;
    private final MethodHandle proxyConstructor;

    AbstractDataObjectCodecContext(final DataContainerCodecPrototype<T> prototype,
            final CodecDataObjectAnalysis<T> analysis) {
        super(prototype);
        byBindingArgClass = analysis.byBindingArgClass;
        byStreamClass = analysis.byStreamClass;
        byYang = analysis.byYang;
        leafChild = analysis.leafNodes;
        proxyConstructor = analysis.proxyConstructor;
    }

    @Override
    public final WithStatus getSchema() {
        // FIXME: Bad cast, we should be returning an EffectiveStatement perhaps?
        return (WithStatus) getType().statement();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <C extends DataObject> DataContainerCodecContext<C, ?> streamChild(final Class<C> childClass) {
        return (DataContainerCodecContext<C, ?>) childNonNull(streamChildPrototype(childClass), childClass,
            "Child %s is not valid child of %s", getBindingClass(), childClass).get();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <C extends DataObject> Optional<DataContainerCodecContext<C, ?>> possibleStreamChild(
            final Class<C> childClass) {
        final var childProto = streamChildPrototype(childClass);
        if (childProto != null) {
            return Optional.of((DataContainerCodecContext<C, ?>) childProto.get());
        }
        return Optional.empty();
    }

    @Nullable DataContainerCodecPrototype<?> streamChildPrototype(final @NonNull Class<?> childClass) {
        return byStreamClass.get(childClass);
    }

    @Override
    public final DataContainerCodecContext<?, ?> bindingPathArgumentChild(final InstanceIdentifier.PathArgument arg,
            final List<PathArgument> builder) {
        final var argType = arg.getType();
        final var context = childNonNull(pathChildPrototype(argType), argType,
            "Class %s is not valid child of %s", argType, getBindingClass())
            .get();
        if (context instanceof ChoiceNodeCodecContext<?> choice) {
            choice.addYangPathArgument(arg, builder);

            final var caseType = arg.getCaseType();
            final var type = arg.getType();
            final DataContainerCodecContext<?, ?> caze;
            if (caseType.isPresent()) {
                // Non-ambiguous addressing this should not pose any problems
                caze = choice.streamChild(caseType.orElseThrow());
            } else {
                caze = choice.getCaseByChildClass(type);
            }

            caze.addYangPathArgument(arg, builder);
            return caze.bindingPathArgumentChild(arg, builder);
        }
        context.addYangPathArgument(arg, builder);
        return context;
    }

    @Nullable DataContainerCodecPrototype<?> pathChildPrototype(final @NonNull Class<? extends DataObject> argType) {
        return byBindingArgClass.get(argType);
    }

    @Override
    public final NodeCodecContext yangPathArgumentChild(final PathArgument arg) {
        NodeContextSupplier supplier;
        if (arg instanceof NodeIdentifier nodeId) {
            supplier = yangChildSupplier(nodeId);
        } else if (arg instanceof NodeIdentifierWithPredicates nip) {
            supplier = yangChildSupplier(new NodeIdentifier(nip.getNodeType()));
        } else {
            supplier = null;
        }
        return childNonNull(supplier, arg, "Argument %s is not valid child of %s", arg, getSchema()).get();
    }

    @Nullable NodeContextSupplier yangChildSupplier(final @NonNull NodeIdentifier arg) {
        return byYang.get(arg);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    final @NonNull D createBindingProxy(final DistinctNodeContainer<?, ?> node) {
        try {
            return (D) proxyConstructor.invokeExact(this, node);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
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

    abstract Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(
        DistinctNodeContainer<PathArgument, NormalizedNode> data);
}
