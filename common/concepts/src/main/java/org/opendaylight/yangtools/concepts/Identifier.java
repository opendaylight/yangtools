/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.io.Serializable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * General identifier interface. It is primarily a marker for all things that identify concepts -- such as names,
 * addresses, classes, etc. We do not require too much, just that the identifiers are serializable (and this
 * transferable).
 *
 * Implementations are expected to implement {@link #hashCode()} and {@link #equals(Object)} methods in a way, which
 * ensures that objects before and after serialization are considered equal.
 *
 * Implementations are advised to use the {@link java.io.Externalizable} Proxy pattern to allow future evolution
 * of their serialization format. For further efficiency, implementation should implement {@link WritableObject},
 * so they can be efficiently embedded in other {@link Serializable} objects.
 *
 * Note that this class is annotated as {@link ThreadSafe}, hence all implementations are expected to be thread-safe.
 */
@ThreadSafe
public interface Identifier extends Serializable, Immutable {
    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}

