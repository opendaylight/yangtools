/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;

/**
 * A prototype for a {@link MapCodecContext}.
 */
final class MapCodecPrototype extends ListCodecPrototype<ListRuntimeType.WithKey> {
    MapCodecPrototype(final DataObjectStep<?> step, final ListRuntimeType.WithKey type,
            final CodecContextFactory factory) {
        super(step, type, factory);
        final var clazz = javaClass();
        checkArgument(KeyAware.class.isAssignableFrom(clazz), "%s is not KeyAware", clazz);
    }

    @Override
    ListCodecContext<?, ListRuntimeType.WithKey> createInstance() {
        return MapCodecContext.of(this);
    }

    @Override
    <T extends CodecDataObject<T>> GenClass<T> generateClass(
            final DataContainerAnalysis<ListRuntimeType.WithKey> analysis) {
        final var runtimeType = runtimeType();
        final var archetype = runtimeType.javaType();
        final var runtimeContext = contextFactory().runtimeContext();

        final Class<? extends Key<?>> keyClass;
        try {
            keyClass = runtimeContext.loadClass(archetype.keyName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load key class for " + javaClass(), e);
        }
//        final var parentClass = runtimeContext.loadClass(archetype.parentName());


        return generateEntryObject(analysis, runtimeType(), keyClass);
    }

}
