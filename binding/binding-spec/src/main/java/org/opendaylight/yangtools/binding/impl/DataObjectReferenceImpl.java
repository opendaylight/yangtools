/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataContainer.Addressable;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.KeyStep;

public sealed class DataObjectReferenceImpl<T extends Addressable>
        extends AbstractDataObjectReference<T, DataObjectStep<?>> permits DataObjectReferenceWithKey {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public DataObjectReferenceImpl(final Iterable<? extends @NonNull DataObjectStep<?>> steps) {
        super(steps);
    }

    public static final @NonNull DataObjectReferenceImpl<?> ofUnsafeSteps(
            final ImmutableList<? extends @NonNull DataObjectStep<?>> steps) {
        return steps.getLast() instanceof KeyStep ? new DataObjectReferenceWithKey<>(steps)
            : new DataObjectReferenceImpl<>(steps);
    }

    @Override
    public AbstractDataObjectReferenceBuilder<T> toBuilder() {
        return new DataObjectReferenceBuilder<>(this);
    }

    @Override
    public DataObjectIdentifier<T> toIdentifier() {
        throw new UnsupportedOperationException(this + " contains inexact steps");
    }

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throwNSE();
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        throwNSE();
    }

    @java.io.Serial
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throwNSE();
    }
}
