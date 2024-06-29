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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataContainer.Addressable;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.KeyStep;

public sealed class DataObjectIdentifierImpl<T extends Addressable>
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
    public AbstractDataObjectReferenceBuilder<T> toBuilder() {
        return new DataObjectReferenceBuilder<>(this);
    }

    @Override
    public DataObjectIdentifierImpl<T> toIdentifier() {
        return this;
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
