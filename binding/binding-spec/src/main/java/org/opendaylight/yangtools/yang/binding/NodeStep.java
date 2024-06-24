/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.io.ObjectStreamException;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.KeyAware;

/**
 * A {@link DataObject}-based step along an {@code instance-identifier}. It equates to a {@code node-identifier} and
 * carries additional assertion that there are no valid predicates for this type.
 *
 * @param <T> DataObject type
 */
@Deprecated(since = "14.0.0", forRemoval = true)
record NodeStep<T extends DataObject>(
        @NonNull Class<T> type,
        @Nullable Class<? extends DataObject> caseType) implements Serializable {
    NodeStep {
        checkType(type, false);
        checkCaseType(caseType);
    }

    @java.io.Serial
    Object readResolve() throws ObjectStreamException {
        return new org.opendaylight.yangtools.binding.NodeStep<>(type, caseType);
    }

    static void checkType(final Class<?> type, final boolean keyAware) {
        if (!DataObject.class.isAssignableFrom(type) || KeyAware.class.isAssignableFrom(type) != keyAware) {
            throw new IllegalArgumentException("Invalid type " + type);
        }
    }

    static void checkCaseType(final @Nullable Class<?> caseType) {
        if (caseType != null && !DataObject.class.isAssignableFrom(caseType)) {
            throw new IllegalArgumentException("Invalid case type " + caseType);
        }
    }
}
