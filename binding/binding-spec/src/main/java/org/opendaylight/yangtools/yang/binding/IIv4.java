/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;

sealed class IIv4<T extends DataObject> implements Externalizable permits KIIv4 {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private @Nullable Iterable<DataObjectStep<?>> pathArguments;
    private @Nullable Class<T> targetType;
    private boolean wildcarded;
    private int hash;

    @SuppressWarnings("redundantModifier")
    public IIv4() {
        // For Externalizable
    }

    IIv4(final InstanceIdentifier<T> source) {
        pathArguments = source.pathArguments;
        targetType = source.getTargetType();
        wildcarded = source.isWildcarded();
        hash = source.hashCode();
    }

    final int getHash() {
        return hash;
    }

    final Iterable<DataObjectStep<?>> getPathArguments() {
        return pathArguments;
    }

    final Class<T> getTargetType() {
        return targetType;
    }

    final boolean isWildcarded() {
        return wildcarded;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(targetType);
        out.writeBoolean(wildcarded);
        out.writeInt(hash);
        out.writeInt(Iterables.size(pathArguments));
        for (var o : pathArguments) {
            out.writeObject(o);
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        targetType = (Class<T>) in.readObject();
        wildcarded = in.readBoolean();
        hash = in.readInt();

        final int size = in.readInt();
        final var builder = ImmutableList.<DataObjectStep<?>>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            builder.add((DataObjectStep<?>) in.readObject());
        }
        pathArguments = builder.build();
    }

    @java.io.Serial
    Object readResolve() throws ObjectStreamException {
        return new InstanceIdentifier<>(targetType, pathArguments, wildcarded, hash);
    }
}
