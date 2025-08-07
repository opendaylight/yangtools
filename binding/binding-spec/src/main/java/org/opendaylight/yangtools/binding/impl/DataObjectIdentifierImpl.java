/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.KeyStep;

public sealed class DataObjectIdentifierImpl<T extends DataObject>
        extends AbstractDataObjectReference<T, ExactDataObjectStep<?>> implements DataObjectIdentifier<T>
        permits DataObjectIdentifierWithKey {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    DataObjectIdentifierImpl(final Iterable<? extends @NonNull ExactDataObjectStep<?>> steps) {
        super(steps);
    }

    public DataObjectIdentifierImpl(final Void unused, final Iterable<? extends @NonNull DataObjectStep<?>> steps) {
        this(verifySteps(steps));
    }

    @SuppressWarnings("unchecked")
    private static @NonNull Iterable<? extends @NonNull ExactDataObjectStep<?>> verifySteps(
            final Iterable<? extends @NonNull DataObjectStep<?>> steps) {
        steps.forEach(step -> verify(step instanceof ExactDataObjectStep, "%s is not an exact step", step));
        return (Iterable<? extends @NonNull ExactDataObjectStep<?>>) steps;
    }

    public static final @NonNull DataObjectIdentifierImpl<?> ofUnsafeSteps(
            final ImmutableList<? extends @NonNull ExactDataObjectStep<?>> steps) {
        final var last = steps.getLast();
        return last instanceof KeyStep ? new DataObjectIdentifierWithKey<>(steps)
            : new DataObjectIdentifierImpl<>(steps);
    }

    @Override
    public AbstractDataObjectIdentifierBuilder<T> toBuilder() {
        return new DataObjectIdentifierBuilder<>(this);
    }

    @Override
    public final <I extends DataObject> DataObjectIdentifier<I> tryTrimTo(final Class<@NonNull I> type) {
        final var casted = type.asSubclass(DataObject.class);
        final var steps = steps();

        int count = 1;
        for (var step : steps) {
            if (casted.equals(step.type())) {
                @SuppressWarnings("unchecked")
                final var ret = (DataObjectIdentifier<I>) DataObjectIdentifier.ofUnsafeSteps(
                    Iterables.limit(steps, count));
                return ret;
            }

            ++count;
        }

        return null;
    }

    @Override
    protected final Class<?> contract() {
        return DataObjectIdentifier.class;
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
