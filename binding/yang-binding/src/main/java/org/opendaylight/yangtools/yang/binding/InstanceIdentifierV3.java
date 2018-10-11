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
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

class InstanceIdentifierV3<T extends DataObject> implements Externalizable {
    private static final long serialVersionUID = 3L;

    private @Nullable Iterable<PathArgument> pathArguments;
    private @Nullable Class<T> targetType;
    private boolean wildcarded;
    private int hash;

    @SuppressWarnings("redundantModifier")
    public InstanceIdentifierV3() {
        // For Externalizable
    }

    InstanceIdentifierV3(final InstanceIdentifier<T> source) {
        pathArguments = source.pathArguments;
        targetType = source.getTargetType();
        wildcarded = source.isWildcarded();
        hash = source.hashCode();
    }

    final int getHash() {
        return hash;
    }

    final Iterable<PathArgument> getPathArguments() {
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
        for (Object o : pathArguments) {
            out.writeObject(o);
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        targetType = (Class<T>) in.readObject();
        wildcarded = in.readBoolean();
        hash = in.readInt();

        final int size = in.readInt();
        final List<PathArgument> args = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            args.add((PathArgument) in.readObject());
        }
        pathArguments = ImmutableList.copyOf(args);
    }

    Object readResolve() throws ObjectStreamException {
        return new InstanceIdentifier<>(targetType, pathArguments, wildcarded, hash);
    }
}
