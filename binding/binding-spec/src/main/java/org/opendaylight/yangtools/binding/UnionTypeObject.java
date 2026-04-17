/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link TypeObject} corresponding to a {@code type union}.
 */
@NonNullByDefault
public non-sealed interface UnionTypeObject extends TypeObject {
    /**
     * {@return the {@link UnionValue} stored in this object}
     * @since 16.0.0
     */
    UnionValue<?> value();
}
