/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.EnumTypeObject;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractEnumTypeValue;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;

/**
 * A single value of an {@link EnumTypeObject}.
 */
@NonNullByDefault
public sealed interface EnumTypeValue extends Immutable, DocumentedNode.WithStatus permits AbstractEnumTypeValue {
    /**
     * {@return the integer value assigned to this member}
     */
    int value();

    /**
     * {@return the name assigned to this member}
     */
    String name();

    /**
     * {@return the name of Java {@code enum} constant assigned to this member}
     */
    String constantName();
}
