/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Utility interface, which specializes {@link UncheckedCodec} to {@link IllegalArgumentException}. This is useful
 * for migration purposes. Implementations should consider subclassing {@link AbstractIllegalArgumentCodec}.
 *
 * @param <S> Serializied (external) type
 * @param <D> Deserialized (internal) type
 */
@Beta
public interface IllegalArgumentCodec<S, D> {
    /**
     * Produce an object base on input.
     *
     * @param input Input object
     * @return Product derived from input
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException when input is not valid
     */
    @NonNull D deserialize(@NonNull S input);

    /**
     * Convert an input into a product.
     *
     * @param input Input
     * @return An external form object
     * @throws NullPointerException if input is null
     * @throws IllegalArgumentException when input is not valid
     */
    @NonNull S serialize(@NonNull D input);
}
