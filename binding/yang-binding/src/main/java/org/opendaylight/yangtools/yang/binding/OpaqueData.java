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
}
