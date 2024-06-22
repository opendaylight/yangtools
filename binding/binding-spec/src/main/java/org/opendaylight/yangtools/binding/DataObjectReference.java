/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A reference to a {@link DataObject} with semantics partially overlapping with to YANG {@code instance-identifier}.
 */
public abstract sealed class DataObjectReference<S extends DataObjectStep<?>, T extends DataObject>
        implements Immutable, Serializable
        permits InstanceIdentifier, DataObjectIdentifier {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    /**
     * Return the steps of this reference. Returned {@link Iterable} does not support removals and contains one or more
     * non-null ite,s.
     *
     * @return the steps of this reference
     */
    public abstract @NonNull Iterable<@NonNull S> steps();

    public abstract @NonNull DataObjectStep<T> lastStep();

    /**
     * Create a {@link DataObjectReferenceBuilder} producing equivalent reference.
     *
     * @return A builder instance
     */
    public abstract @NonNull DataObjectReferenceBuilder<S, T> toBuilder();

    static final @NonNull DataObjectReference<?, ?> ofSteps(final ImmutableList<? extends DataObjectStep<?>> steps) {
        return steps.stream().anyMatch(InexactDataObjectStep.class::isInstance) ? InstanceIdentifier.unsafeOf(steps)
            : DataObjectIdentifier.unsafeOf((ImmutableList<ExactDataObjectStep<?>>) steps);
    }

    @Override
    public final int hashCode() {
        int hashCode = 1;
        for (var element : steps()) {
            hashCode = hashCode * 31 + element.hashCode();
        }
        return hashCode;
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj != null && getClass().equals(obj.getClass())
            && Iterables.elementsEqual(steps(), ((DataObjectReference<?, ?>) obj).steps());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(contract())).toString();
    }

    /**
     * Add class-specific toString attributes.
     *
     * @param toStringHelper ToStringHelper instance
     * @return ToStringHelper instance which was passed in
     */
    ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        // FIXME: add steps
        return toStringHelper;
    }

    abstract @NonNull Class<?> contract();

    @java.io.Serial
    final Object writeReplace() throws ObjectStreamException {
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

    final void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(getClass().getName());
    }
}
