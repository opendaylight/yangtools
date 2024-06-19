/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link DataObject}-based step along an {@code instance-identifier}. It equates to a {@code node-identifier} and
 * carries additional assertion that there are no valid predicates for this type.
 *
 * @param <T> DataObject type
 */
public record NodeStep<T extends DataObject>(
        @NonNull Class<T> type,
        @Nullable Class<? extends DataObject> caseType) implements ExactDataObjectStep<T> {
    public NodeStep {
        checkType(type, false);
        checkCaseType(caseType);
    }

    public NodeStep(final @NonNull Class<T> type) {
        this(type, null);
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
