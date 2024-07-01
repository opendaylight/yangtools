/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static java.util.Objects.requireNonNull;

import java.io.ObjectStreamException;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Serialization proxy of {@link KeyStep}. Named after {@code Keyed Entry Version 1}.
 */
record KEv1<K extends Key<T>, T extends EntryObject<T, K>>(
        @NonNull Class<T> type,
        @Nullable Class<? extends DataObject> caseType,
        @NonNull K key) implements Serializable {
    KEv1 {
        requireNonNull(type);
        requireNonNull(key);
    }

    @java.io.Serial
    Object readResolve() throws ObjectStreamException {
        return new KeyStep<>(type, caseType, key);
    }
}
