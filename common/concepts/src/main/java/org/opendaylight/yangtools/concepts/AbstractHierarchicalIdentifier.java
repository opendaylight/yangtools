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
import org.eclipse.jdt.annotation.NonNull;

/**
 * An opinionated superclass for implementing {@link HierarchicalIdentifier}s.
 *
 * <p>
 * It assumes that the identifier is composed of multiple non-null steps available via {@link #itemIterator()} and that
 * {@link #contains(AbstractHierarchicalIdentifier)} semantics can be implemented using simple in-order comparison of
 * these steps.
 *
 * <p>
 * Furthermore it mandates that serialization occurs via {@link #writeReplace()}, following the Serialization Proxy
 * pattern.
 */
public abstract class AbstractHierarchicalIdentifier<T extends AbstractHierarchicalIdentifier<T, I>, I>
        implements HierarchicalIdentifier<T> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @Override
    public final boolean contains(final T other) {
        if (this != other) {
            final var oit = other.itemIterator();
            final var it = itemIterator();
            while (it.hasNext()) {
                if (!oit.hasNext() || !it.next().equals(oit.next())) {
                    return false;
                }
            }
        }
        return true;
    }

    protected abstract @NonNull Iterator<@NonNull I> itemIterator();

    @java.io.Serial
    protected abstract @NonNull Object writeReplace() throws ObjectStreamException;

    /**
     * Utility method throwing a {@link NotSerializableException}. It is useful when implementing
     * {@link #readObject(ObjectInputStream)}, {@link #readObjectNoData()} and {@link #writeObject(ObjectOutputStream)}
     * methods, which all subclasses should define as serialization is driven via {@link #writeReplace()}.
     *
     * @throws NotSerializableException always
     */
    protected final void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(getClass().getName());
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

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
