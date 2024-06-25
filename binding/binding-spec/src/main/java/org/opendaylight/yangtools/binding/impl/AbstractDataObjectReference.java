/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
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

    // FIXME: YANGTOOLS-1577: final
    @Override
    public abstract Iterable<? extends @NonNull S> steps();

    // FIXME: YANGTOOLS-1577: final
    @Override
    public abstract int hashCode();

    // FIXME: YANGTOOLS-1577: final
    @Override
    public abstract boolean equals(Object obj);

    @Override
    public final String toString() {
        // FIXME: YANGTOOLS-1577: pretty-print steps instead
        return addToStringAttributes(MoreObjects.toStringHelper(contract())).toString();
    }

    protected @NonNull Class<?> contract() {
        return DataObjectReference.class;
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
