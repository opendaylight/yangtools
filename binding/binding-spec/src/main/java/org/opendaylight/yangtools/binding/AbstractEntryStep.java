/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

abstract sealed class AbstractEntryStep<T extends EntryObject<?, ?>> implements DataObjectStep<T>
        permits KeyStep, KeylessStep {
    private static final long serialVersionUID = 1L;

    private final @NonNull Class<T> type;
    private final @Nullable Class<? extends DataObject> caseType;

    AbstractEntryStep(final boolean verify, final @NonNull Class<T> type,
            final @Nullable Class<? extends DataObject> caseType) {
        this.type = requireNonNull(type);
        this.caseType = caseType;
        if (verify) {
            NodeStep.checkType(type, true);
            NodeStep.checkCaseType(caseType);
        }
    }

    @Override
    public final Class<T> type() {
        return type;
    }

    @Override
    public final Class<? extends DataObject> caseType() {
        return caseType;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("case", caseType).add("type", type);
    }

    @java.io.Serial
    final Object writeReplace() throws ObjectStreamException {
        return toSerialForm();
    }

    abstract @NonNull Object toSerialForm();
}
