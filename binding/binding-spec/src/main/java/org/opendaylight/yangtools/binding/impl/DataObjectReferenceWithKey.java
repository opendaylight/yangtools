/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectReference.WithKey;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.KeyStep;

public final class DataObjectReferenceWithKey<T extends KeyAware<K> & DataObject, K extends Key<T>>
        extends DataObjectReferenceImpl<T> implements WithKey<T, K> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public DataObjectReferenceWithKey(final Iterable<? extends @NonNull DataObjectStep<?>> steps) {
        super(steps);
    }

    @Override
    public KeyStep<K, T> lastStep() {
        return getLast(steps());
    }

    @Override
    public DataObjectReferenceBuilderWithKey<T, K> toBuilder() {
        return new DataObjectReferenceBuilderWithKey<>(this);
    }

    @Override
    public DataObjectIdentifier.WithKey<T, K> toIdentifier() {
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
