/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.Iterator;
import java.util.Objects;

/**
 * An opinioned superclass for implementing {@link HierarchicalIdentifier}s.
 */
public abstract class AbstractHierarchicalIdentifier<T extends AbstractHierarchicalIdentifier<T>>
        implements HierarchicalIdentifier<T> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Override
    public final boolean contains(final T other) {
        if (this != other) {
            final var oit = other.itemIterator();
            final var it = itemIterator();
            while (it.hasNext()) {
                if (!oit.hasNext() || !Objects.equals(it.next(), oit.next())) {
                    return false;
                }
            }
        }
        return true;
    }

    protected abstract Iterator<?> itemIterator();

    @java.io.Serial
    protected abstract Object writeReplace() throws ObjectStreamException;

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    @java.io.Serial
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throw nse();
    }

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throw nse();
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        throw nse();
    }

    private NotSerializableException nse() {
        return new NotSerializableException(getClass().getName());
    }
}
