/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link EntryObject}-based step without the corresponding key. This corresponds to a {@code node-identifier} step,
 * where we know there is a {@code key-predicate} possible, but we do not have it.
 *
 * @param <T> EntryObject type
 */
public record KeylessStep<T extends EntryObject<?, ?>>(
        @NonNull Class<T> type,
        @Nullable Class<? extends DataObject> caseType) implements InexactDataObjectStep<T> {
    public KeylessStep {
        NodeStep.checkType(type, true);
        NodeStep.checkCaseType(caseType);
    }

    public KeylessStep(final @NonNull Class<T> type) {
        this(type, null);
    }

    @Override
    public boolean matches(final DataObjectStep<?> other) {
        // FIXME: this should be an instanceof check for KeyStep, then a match -- i.e. reject match on plain NodeStep,
        //        because that is an addressing mismatch
        return type.equals(other.type()) && Objects.equals(caseType, other.caseType());
    }
}
