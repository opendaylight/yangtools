/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thrown when Java Binding class was used in data for which codec does not have schema.
 *
 * <p>By serialization /  deserialization of this exception {@link #getBindingClass()} will return null.
 */
@Beta
public final class MissingSchemaForClassException extends MissingSchemaException {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    // It's either this or SuppressFBWarnings
    @SuppressWarnings("checkstyle:mutableException")
    private transient Class<?> bindingClass;

    public MissingSchemaForClassException(final Class<?> clz) {
        super(String.format("Schema is not available for %s", clz));
        bindingClass = requireNonNull(clz);
    }

    public @Nullable Class<?> getBindingClass() {
        return bindingClass;
    }

    @java.io.Serial
    private Object readResolve() {
        bindingClass = null;
        return this;
    }
}
