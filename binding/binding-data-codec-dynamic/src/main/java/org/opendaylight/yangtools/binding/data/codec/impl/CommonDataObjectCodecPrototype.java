/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.AugmentableRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;

/**
 * Common superclass for {@link DataObjectCodecPrototype} and {@link AugmentationCodecPrototype}.
 *
 * @param <R> {@link CompositeRuntimeType} type
 */
abstract sealed class CommonDataObjectCodecPrototype<R extends CompositeRuntimeType>
        extends DataContainerPrototype<CommonDataObjectCodecContext<?, R>, R>
        permits AugmentationCodecPrototype, DataObjectCodecPrototype {
    record GenClass<T extends CodecDataObject<T>>(
            @NonNull Class<T> clazz,
            @NonNull List<AugmentRuntimeType> possibleAugmentations) {
        GenClass {
            requireNonNull(clazz);
            requireNonNull(possibleAugmentations);
        }
    }

    private final @NonNull DataObjectStep<?> step;

    CommonDataObjectCodecPrototype(final DataObjectStep<?> step, final R runtimeType,
            final CodecContextFactory factory) {
        super(factory, runtimeType);
        this.step = requireNonNull(step);
    }

    @Override
    final Class<? extends DataObject> javaClass() {
        return step.type();
    }

    final @NonNull DataObjectStep<?> getBindingArg() {
        return step;
    }

    // FIXME: this bit is not nice but it works

    abstract <T extends CodecDataObject<T>> GenClass<T> generateClass(DataContainerAnalysis<R> analysis);

    final <T extends CodecDataObject<T>> GenClass<T> generate(final DataContainerAnalysis<R> analysis) {
        return new GenClass<>(
            CodecDataObjectGenerator.<T>generate(contextFactory().getLoader(), javaClass(),
                analysis.leafContexts, analysis.daoProperties),
            List.of());
    }

    final <T extends CodecDataObject<T>> GenClass<T> generateAugmentable(final DataContainerAnalysis<R> analysis,
            final AugmentableRuntimeType runtimeType) {
        return new GenClass<>(
            CodecDataObjectGenerator.<T>generateAugmentable(contextFactory().getLoader(), javaClass(),
                analysis.leafContexts, analysis.daoProperties),
            runtimeType.augments());
    }

    final <T extends CodecDataObject<T>> GenClass<T> generateEntryObject(final DataContainerAnalysis<R> analysis,
            final ListRuntimeType.WithKey runtimeType, final Class<? extends Key<?>> keyClass) {
        return new GenClass<>(
            CodecEntryObjectGenerator.<T>generate(contextFactory().getLoader(), javaClass(),
                analysis.leafContexts, analysis.daoProperties, keyClass),
            runtimeType.augments());
    }
}
