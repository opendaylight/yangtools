/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Thrown when Java Binding class was used in data for which codec does not have schema.
 *
 * <p>
 * By serialization /  deserialization of this exception {@link #getBindingClass()} will return null.
 */
public final class MissingSchemaForClassException extends MissingSchemaException {

    private static final long serialVersionUID = 1L;

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private final transient Class<?> bindingClass;

    private MissingSchemaForClassException(final Class<?> clz) {
        super(String.format("Schema is not available for %s", clz));
        this.bindingClass = requireNonNull(clz);
    }

    static MissingSchemaForClassException forClass(final Class<?> clz) {
        return new MissingSchemaForClassException(clz);
    }

    public Class<?> getBindingClass() {
        return bindingClass;
    }

    public static void check(final BindingRuntimeContext runtimeContext, final Class<?> bindingClass) {
        final Object schema;
        if (Augmentation.class.isAssignableFrom(bindingClass)) {
            schema = runtimeContext.getAugmentationDefinition(bindingClass);
        } else {
            schema = runtimeContext.getSchemaDefinition(bindingClass);
        }
        if (schema == null) {
            throw MissingSchemaForClassException.forClass(bindingClass);
        }
    }
}
