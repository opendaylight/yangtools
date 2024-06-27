/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Base implementation of {@link DataObjectReference}.
 */
public abstract sealed class AbstractDataObjectReference<T extends DataObject, S extends DataObjectStep<?>>
        implements DataObjectReference<T>
        permits DataObjectIdentifierImpl, DataObjectReferenceImpl, InstanceIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull Iterable<? extends @NonNull S> steps;

    protected AbstractDataObjectReference(final Iterable<? extends @NonNull S> steps) {
        this.steps = requireNonNull(steps);
    }

    @Override
    public final Iterable<? extends @NonNull S> steps() {
        return steps;
    }

    @Override
    public DataObjectStep<T> lastStep() {
        return getLast(steps);
    }

    @Override
    public final int hashCode() {
        int hash = 1;
        for (var step : steps) {
            hash = 31 * hash + step.hashCode();
        }
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj instanceof AbstractDataObjectReference other
            && Iterables.elementsEqual(steps, other.steps);
    }

    @Override
    public final String toString() {
        // FIXME: YANGTOOLS-1577: pretty-print steps instead
        return addToStringAttributes(MoreObjects.toStringHelper(contract())).toString();
    }

    protected @NonNull Class<?> contract() {
        return DataObjectReference.class;
    }

    @SuppressWarnings("unchecked")
    protected static final <T> @NonNull T getLast(final Iterable<?> steps) {
        return (@NonNull T) switch (steps) {
            case AppendIterable<?> append -> append.last();
            case List<?> list -> list.getLast();
            default -> Iterables.getLast(steps);
        };
    }

    /**
     * Add class-specific toString attributes.
     *
     * @param toStringHelper ToStringHelper instance
     * @return ToStringHelper instance which was passed in
     */
    protected abstract ToStringHelper addToStringAttributes(ToStringHelper toStringHelper);

    protected final void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(getClass().getName());
    }

    @java.io.Serial
    protected final Object writeReplace() throws ObjectStreamException {
        return toSerialForm();
    }

    protected @NonNull Object toSerialForm() {
        return new ORv1(this);
    }

    @NonNullByDefault
    public static final <T> Iterable<? extends T> concat(final Iterable<? extends T> others, final T last) {
        return new AppendIterable<>(others, last);
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
