/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Generated Property is essentially a named field with its type.
 */
@Deprecated(since = "16.0.0", forRemoval = true)
@NonNullByDefault
interface GeneratedProperty {
    /**
     * {@return the name of this property}
     */
    @Deprecated(since = "16.0.0", forRemoval = true)
    String getName();

    /**
     * {@return the returning {@link Type} of member}
     */
    @Deprecated(since = "16.0.0", forRemoval = true)
    Type getReturnType();

    /**
     * Returns <code>true</code> if the property is declared as read-only. If this {@code true} the property should be
     * generated with only a getter.
     *
     * @return {@code true<} if the property is declared as read-only.
     */
    @Deprecated(since = "16.0.0", forRemoval = true)
    boolean isReadOnly();
}
