/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thrown when Java Binding class was used in data for which codec does not have schema.
 *
 * <p>
 * By serialization /  deserialization of this exception {@link #getBindingClass()} will return null.
 */
@Beta
public final class MissingSchemaForClassException extends MissingSchemaException {
    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED", justification = "Documented in API contract")
    private final transient Class<?> bindingClass;

    public MissingSchemaForClassException(final Class<?> clz) {
        super(String.format("Schema is not available for %s", clz));
        this.bindingClass = requireNonNull(clz);
    }

    public @Nullable Class<?> getBindingClass() {
        return bindingClass;
    }
}
