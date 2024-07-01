/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.ContractTrust;
import org.opendaylight.yangtools.binding.impl.CodegenTrust;

/**
 * A {@link EntryObject}-based step without the corresponding key. This corresponds to a {@code node-identifier} step,
 * where we know there is a {@code key-predicate} possible, but we do not have it.
 *
 * @param <T> EntryObject type
 */
public final class KeylessStep<K extends Key<T>, T extends EntryObject<T, K>> extends AbstractEntryStep<T>
        implements InexactDataObjectStep<T> {
    public KeylessStep(final ContractTrust trust, final @NonNull Class<T> type,
            final @Nullable Class<? extends DataObject> caseType) {
        super(CodegenTrust.UNTRUSTED, type, caseType);
        requireNonNull(trust);
    }

    public KeylessStep(final ContractTrust trust, final @NonNull Class<T> type) {
        super(CodegenTrust.UNTRUSTED, type, null);
        requireNonNull(trust);
    }

    public KeylessStep(final @NonNull Class<T> type, final @Nullable Class<? extends DataObject> caseType) {
        super(true, type, caseType);
    }

    public KeylessStep(final @NonNull Class<T> type) {
        this(type, null);
    }

    @Override
    public boolean matches(final DataObjectStep<?> other) {
        // FIXME: this should be an instanceof check for KeyStep, then a match -- i.e. reject match on plain NodeStep,
        //        because that is an addressing mismatch
        return type().equals(other.type()) && Objects.equals(caseType(), other.caseType());
    }

    @Override
    Object toSerialForm() {
        return new AEv1<>(type(), caseType());
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

    private static void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(KeyStep.class.getName());
    }
}
