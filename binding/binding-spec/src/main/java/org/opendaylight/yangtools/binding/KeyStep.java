/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
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
 * A {@link EntryObject}-based step with a {@link #key()}. It equates to a {@code node-identifier} with a
 * {@code key-predicate}.
 *
 * @param <K> Key type
 * @param <T> KeyAware type
 */
public final class KeyStep<K extends Key<T>, T extends EntryObject<T, K>> extends AbstractEntryStep<T>
        implements ExactDataObjectStep<T>, KeyAware<K> {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull K key;

    private KeyStep(final boolean verify, final @NonNull Class<T> type,
            final @Nullable Class<? extends DataObject> caseType, final @NonNull K key) {
        super(verify, type, caseType);
        this.key = requireNonNull(key);
    }

    public KeyStep(final ContractTrust trust, final @NonNull Class<T> type,
            final @Nullable Class<? extends DataObject> caseType, final @NonNull K key) {
        this(CodegenTrust.UNTRUSTED, type, caseType, key);
        requireNonNull(trust);
    }

    public KeyStep(final @NonNull Class<T> type, final @Nullable Class<? extends DataObject> caseType,
            final @NonNull K key) {
        this(true, type, caseType, key);
    }

    public KeyStep(final @NonNull Class<T> type, final @NonNull K key) {
        this(type, null, key);
    }

    @Override
    public K key() {
        return key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseType(), type(), key);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof KeyStep other
            && type().equals(other.type()) && key.equals(other.key) && Objects.equals(caseType(), other.caseType());
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("key", key);
    }

    @Override
    Object toSerialForm() {
        return new KEv1<>(type(), caseType(), key);
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