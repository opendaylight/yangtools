/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.collect.ImmutableList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;

@Deprecated(since = "14.0.0", forRemoval = true)
sealed class IIv4<T extends DataObject> implements Externalizable permits KIIv4 {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private @Nullable ImmutableList<? extends DataObjectStep<?>> pathArguments;

    @SuppressWarnings("redundantModifier")
    public IIv4() {
        // For Externalizable
    }

    @Override
    public final void writeExternal(final ObjectOutput out) throws IOException {
        throw new NotSerializableException(getClass().getName());
    }

    @Override
    @SuppressWarnings("ReturnValueIgnored")
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        Class.class.cast(in.readObject());
        in.readBoolean();
        in.readInt();

        final int size = in.readInt();
        final var builder = ImmutableList.<DataObjectStep<?>>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            builder.add((DataObjectStep<?>) in.readObject());
        }
        pathArguments = builder.build();
    }

    @java.io.Serial
    final Object readResolve() throws ObjectStreamException {
        return InstanceIdentifier.unsafeOf(pathArguments);
    }
}
