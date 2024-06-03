/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link KeyAware}-based step with a {@link #key()}. It equates to a {@code node-identifier} with a
 * {@code key-predicate}.
 *
 * @param <K> Key type
 * @param <T> KeyAware type
 */
public record KeyStep<K extends Key<T>, T extends KeyAware<K> & DataObject>(
        @NonNull Class<T> type,
        @Nullable Class<? extends DataObject> caseType,
        @NonNull K key) implements ExactDataObjectStep<T>, KeyAware<K> {
    public KeyStep {
        NodeStep.checkType(type, true);
        NodeStep.checkCaseType(caseType);
        requireNonNull(key);
    }

    public KeyStep(final @NonNull Class<T> type, final @NonNull K key) {
        this(type, null, key);
    }
}
