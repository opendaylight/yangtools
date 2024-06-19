/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An immutable view of data. Data representation identity is available through {@link #getObjectModel()} and the actual
 * data is available through {@link #getData()}.
 *
 * @param <T> Data object model type
 */
@Beta
public interface OpaqueData<T> extends Immutable {
    /**
     * Return the object model class, which identifies it.
     *
     * @return Object model class
     */
    @NonNull Class<T> getObjectModel();

    /**
     * Return the data in associated object model.
     *
     * @return Data held in this object.
     */
    @NonNull T getData();

    /**
     * The hash code of any {@link OpaqueData} instance is defined by the combination of its object model and the data
     * it holds. This is inherently object-model-specific hence different OpaqueData defined by distinct object models
     * will result in differing hash codes. This implies that node with differing object models cannot compare as equal.
     * See {@link AbstractOpaqueData#hashCode()} for canonical implementation.
     *
     * @return a hash code value for this object.
     */
    @Override
    int hashCode();

    /**
     * Compare this object to another object. The comparison needs to take into account {@link #getObjectModel()}
     * first and then follow comparison on {@link #getData()}. For canonical algorithm please refer to
     * {@link AbstractOpaqueData#equals(Object)}.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    boolean equals(Object obj);
}
