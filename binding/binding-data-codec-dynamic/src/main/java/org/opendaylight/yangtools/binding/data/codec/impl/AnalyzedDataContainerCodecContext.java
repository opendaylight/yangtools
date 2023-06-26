/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.data.codec.api.IncorrectNestingException;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A {@link DataContainerCodecContext} with an associated {@link DataContainerAnalysis}.
 */
abstract sealed class AnalyzedDataContainerCodecContext<D extends DataContainer, R extends CompositeRuntimeType,
        P extends DataContainerPrototype<?, R>>
            extends DataContainerCodecContext<D, R, P>
            permits CommonDataObjectCodecContext, YangDataCodecContext {
    private final @NonNull ImmutableMap<Class<?>, DataContainerPrototype<?, ?>> byBindingArgClass;
    private final @NonNull ImmutableMap<Class<?>, DataContainerPrototype<?, ?>> byStreamClass;
    private final @NonNull ImmutableMap<NodeIdentifier, CodecContextSupplier> byYang;
    private final @NonNull ImmutableMap<String, ValueNodeCodecContext> leafChild;

    AnalyzedDataContainerCodecContext(final P prototype, final DataContainerAnalysis<R> analysis) {
        super(prototype);
        byBindingArgClass = analysis.byBindingArgClass;
        byStreamClass = analysis.byStreamClass;
        byYang = analysis.byYang;
        leafChild = analysis.leafNodes;
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
}
