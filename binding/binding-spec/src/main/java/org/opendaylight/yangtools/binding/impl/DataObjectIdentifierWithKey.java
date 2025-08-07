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
import org.opendaylight.yangtools.binding.DataObjectIdentifier.WithKey;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;

public final class DataObjectIdentifierWithKey<T extends EntryObject<T, K>, K extends Key<T>>
        extends DataObjectIdentifierImpl<T> implements WithKey<T, K> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    DataObjectIdentifierWithKey(final Iterable<? extends @NonNull ExactDataObjectStep<?>> steps) {
        super(steps);
    }

    public DataObjectIdentifierWithKey(final Void unused, final Iterable<? extends @NonNull DataObjectStep<?>> steps) {
        super(unused, steps);
    }

    @Override
    public KeyStep<K, T> lastStep() {
        return getLast(steps());
    }

    @Override
    public DataObjectIdentifierBuilderWithKey<T, K> toBuilder() {
        return new DataObjectIdentifierBuilderWithKey<>(this);
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
