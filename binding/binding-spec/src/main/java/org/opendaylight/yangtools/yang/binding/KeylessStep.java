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
import org.opendaylight.yangtools.binding.EntryObject;

@Deprecated(since = "14.0.0", forRemoval = true)
record KeylessStep<T extends EntryObject<?, ?>>(
        @NonNull Class<T> type,
        @Nullable Class<? extends DataObject> caseType) implements Serializable {
    KeylessStep {
        NodeStep.checkType(type, true);
        NodeStep.checkCaseType(caseType);
    }

    @java.io.Serial
    Object readResolve() throws ObjectStreamException {
        return new org.opendaylight.yangtools.binding.KeylessStep<>(type, caseType);
    }
}
