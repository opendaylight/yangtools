/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.data.codec.api.BindingAugmentationCodecTreeNode;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class AugmentationCodecContext<A extends Augmentation<?>>
        extends CommonDataObjectCodecContext<A, AugmentRuntimeType> implements BindingAugmentationCodecTreeNode<A> {
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class,
        CommonDataObjectCodecContext.class, DataContainerNode.class);
    private static final MethodType AUGMENTATION_TYPE = MethodType.methodType(Augmentation.class,
        AugmentationCodecContext.class, DataContainerNode.class);

    private final MethodHandle proxyConstructor;

    private AugmentationCodecContext(final AugmentationCodecPrototype<A> prototype,
            final DataContainerAnalysis<AugmentRuntimeType> analysis) {
        super(prototype, analysis);

        final var bindingClass = CodecDataObjectGenerator.generate(prototype.contextFactory().getLoader(),
            prototype.javaClass(), analysis.leafContexts, analysis.daoProperties, null);

        final MethodHandle ctor;
        try {
            ctor = MethodHandles.publicLookup().findConstructor(bindingClass, CONSTRUCTOR_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new LinkageError("Failed to find contructor for class " + bindingClass, e);
        }

        proxyConstructor = ctor.asType(AUGMENTATION_TYPE);
    }

    AugmentationCodecContext(final AugmentationCodecPrototype<A> prototype) {
        this(prototype, new DataContainerAnalysis<>(prototype));
    }

    @Override
    public PathArgument serializePathArgument(final DataObjectStep<?> step) {
        if (!bindingArg().equals(step)) {
            throw new IllegalArgumentException("Unexpected argument " + step);
        }
        return null;
    }

    @Override
    public DataObjectStep<?> deserializePathArgument(final PathArgument arg) {
        if (arg != null) {
            throw new IllegalArgumentException("Unexpected argument " + arg);
        }
        return bindingArg();
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    @Override
    public A filterFrom(final DataContainerNode parentData) {
        for (var childArg : ((AugmentationCodecPrototype<?>) prototype()).getChildArgs()) {
            if (parentData.childByArg(childArg) != null) {
                try {
                    return (A) proxyConstructor.invokeExact(this, parentData);
                } catch (final Throwable e) {
                    Throwables.throwIfUnchecked(e);
                    throw new IllegalStateException(e);
                }
            }
        }
        return null;
    }

    @Override
    public ImmutableSet<NodeIdentifier> childPathArguments() {
        return byYangKeySet();
    }

    @Override
    Object deserializeObject(final NormalizedNode normalizedNode) {
        return filterFrom(checkDataArgument(DataContainerNode.class, normalizedNode));
    }

    @Override
    void addYangPathArgument(final List<PathArgument> builder, final DataObjectStep<?> step) {
        // No-op
    }

    @Override
    NodeIdentifier getDomPathArgument() {
        return null;
    }

    @Override
    Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAllAugmentationsFrom(final DataContainerNode data) {
        return Map.of();
    }
}
