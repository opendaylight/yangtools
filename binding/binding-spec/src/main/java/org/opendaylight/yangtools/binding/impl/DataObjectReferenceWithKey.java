/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference.WithKey;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;

// FIXME: YANGTOOLS-1577: final
public abstract non-sealed class DataObjectReferenceWithKey<T extends KeyAware<K> & DataObject, K extends Key<T>>
        extends DataObjectReferenceImpl<T> implements WithKey<T, K> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull K key;

    protected DataObjectReferenceWithKey(final K key) {
        this.key = requireNonNull(key);
    }

    @Override
    public final K key() {
        return key;
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
