/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectReference;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyStep;
import org.opendaylight.yangtools.binding.KeylessStep;
import org.opendaylight.yangtools.binding.NodeStep;
import org.opendaylight.yangtools.binding.contract.Naming;

/**
 * Base implementation of {@link DataObjectReference}.
 */
public abstract sealed class AbstractDataObjectReference<T extends DataObject, S extends DataObjectStep<?>>
        implements DataObjectReference<T>
        permits DataObjectIdentifierImpl, DataObjectReferenceImpl {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final String TRIM_STRING = Naming.PACKAGE_PREFIX + '.';
    private static final int TRIM_LENGTH = TRIM_STRING.length();

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

    // FIXME: final when we do not have InstanceIdentifier
    @Override
    public <N extends EntryObject<N, K>, K extends Key<N>> @Nullable K firstKeyOf(final Class<@NonNull N> listItem) {
        // Guard against nulls and type smuggling
        final var item = listItem.asSubclass(EntryObject.class);

        for (var step : steps) {
            if (step instanceof KeyStep<?, ?> keyStep && item.equals(step.type())) {
                @SuppressWarnings("unchecked")
                final var ret = (K) keyStep.key();
                return ret;
            }
        }
        return null;
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
        return this == obj || obj instanceof AbstractDataObjectReference<?, ?> other
            && Iterables.elementsEqual(steps, other.steps);
    }

    @Override
    public final String toString() {
        final var sb = new StringBuilder(contract().getSimpleName()).append("[\n");
        // Note: invalid start on purpose
        var prevPackage = ".";
        for (var step : steps) {
            prevPackage = appendStep(sb, prevPackage, step);
        }
        return sb.append(']').toString();
    }

    private static @NonNull String appendStep(final StringBuilder sb, final String prevPackage,
            final DataObjectStep<?> step) {
        return switch (step) {
            case KeyStep<?, ?> cast -> appendStep(sb, prevPackage, cast);
            case KeylessStep<?> cast -> appendStep(sb, prevPackage, cast);
            case NodeStep<?> cast -> appendStep(sb, prevPackage, cast);
        };
    }

    private static @NonNull String appendStep(final StringBuilder sb, final String prevPackage,
            final KeyStep<?, ?> step) {
        final var ret = appendStep(sb, prevPackage, step.caseType(), step.type());
        sb.append('[').append(step.key()).append("]\n");
        return ret;
    }

    private static @NonNull String appendStep(final StringBuilder sb, final String prevPackage,
            final KeylessStep<?> step) {
        final var ret = appendStep(sb, prevPackage, step.caseType(), step.type());
        sb.append("(any)\n");
        return ret;
    }

    private static @NonNull String appendStep(final StringBuilder sb, final String prevPackage,
            final NodeStep<?> step) {
        final var ret = appendStep(sb, prevPackage, step.caseType(), step.type());
        sb.append('\n');
        return ret;
    }

    private static @NonNull String appendStep(final StringBuilder sb, final String prevPackage,
            final @Nullable Class<? extends DataObject> caseType, final Class<? extends DataObject> type) {
        sb.append("  ");
        if (caseType != null) {
            appendClass(sb.append('<'), prevPackage, caseType);
            sb.append('>');
        }
        return appendClass(sb, prevPackage, type);
    }

    private static @NonNull String appendClass(final StringBuilder sb, final String trim,
            final Class<? extends DataObject> type) {
        final var fqpn = type.getPackageName();
        if (fqpn.startsWith(trim)) {
            sb.append("... ").append(fqpn, trim.length(), fqpn.length());
        } else if (fqpn.startsWith(TRIM_STRING)) {
            sb.append("@ ").append(fqpn, TRIM_LENGTH, fqpn.length());
        } else {
            sb.append(fqpn);
        }
        sb.append('.').append(type.getSimpleName());
        return fqpn + '.';
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
    protected static final <T> Iterable<? extends T> concat(final Iterable<? extends T> others, final T last) {
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
