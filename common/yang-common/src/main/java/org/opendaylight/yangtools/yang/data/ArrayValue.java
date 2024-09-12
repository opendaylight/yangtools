/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An array value, corresponding to a {@code leaf-list} statement.
 */
@NonNullByDefault
public non-sealed interface ArrayValue<T extends LeafData> extends Value, Iterable<T> {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<ArrayValue> contract() {
        return ArrayValue.class;
    }

    int size();
}
