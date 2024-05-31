/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Common interface for opaque values, corresponding to an {@code anydata} or an {@code anyxml}statement. This interface
 * is meant to be further specialized to individual representation interfaces:
 * <pre>
 *   {@code
 *     public interface FooValue extends OpaqueValue<FooValue> {
 *       @Override
 *       default Class<FooValue> representation() {
 *         return FooValue.class;
 *       }
 *
 *       // ... representation-specific things ...
 *
 *     }
 *   }
 * </pre>
 *
 * @param <T> representation type
 */
public interface OpaqueValue<T extends OpaqueValue<T>> {
    /**
     * Return the representation class of this value.
     *
     * @return the representation class of this value
     */
    @NonNull Class<T> representation();
}
