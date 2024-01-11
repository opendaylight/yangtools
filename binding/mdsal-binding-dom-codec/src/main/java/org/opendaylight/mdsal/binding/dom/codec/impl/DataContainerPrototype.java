/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * A prototype for codecs dealing with {@link DataContainer}s.
 *
 * @param <C> {@link CodecContext} type
 * @param <R> {@link CompositeRuntimeType} type
 */
abstract sealed class DataContainerPrototype<C extends CodecContext, R extends CompositeRuntimeType>
        extends LazyCodecContextSupplier<C> permits CommonDataObjectCodecPrototype {
    private final @NonNull CodecContextFactory contextFactory;
    private final @NonNull R runtimeType;

    DataContainerPrototype(final CodecContextFactory contextFactory, final R runtimeType) {
        this.contextFactory = requireNonNull(contextFactory);
        this.runtimeType = requireNonNull(runtimeType);
    }

    /**
     * Return the {@link CodecContextFactory} associated with this prototype.
     *
     * @return the context factory associated with this prototype
     */
    final @NonNull CodecContextFactory contextFactory() {
        return contextFactory;
    }

    /**
     * Return associated run-time type.
     *
     * @return associated run-time type
     */
    final @NonNull R runtimeType() {
        return runtimeType;
    }

    /**
     * Return the generated binding class this prototype corresponds to.
     *
     * @return the generated binding class this prototype corresponds to
     */
    abstract @NonNull Class<? extends DataContainer> javaClass();
}
