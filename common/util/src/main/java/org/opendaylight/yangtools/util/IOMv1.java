/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Base class for {@link ImmutableOffsetMap} serialization proxies. Implements most of the serialization form at logic.
 */
abstract sealed class IOMv1<T extends ImmutableOffsetMap<?, ?>> implements Externalizable permits OIOMv1, UIOMv1 {
    @java.io.Serial
    private static final long serialVersionUID = 1;

    private ImmutableOffsetMap<?, ?> map;

    IOMv1() {
        // For Externalizable
    }

    IOMv1(final @NonNull T map) {
        this.map = requireNonNull(map);
    }

    @Override
    public final void writeExternal(final ObjectOutput out) throws IOException {
        final var local = verifyNotNull(map);
        out.writeInt(local.size());
        for (var e : local.entrySet()) {
            out.writeObject(e.getKey());
            out.writeObject(e.getValue());
        }
    }

    @Override
    public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        if (map == null) {
            // TODO: optimize for size == 1? what can we gain?
            final int size = in.readInt();
            final var keysBuilder = ImmutableList.builderWithExpectedSize(size);
            final var values = new Object[size];
            for (int i = 0; i < size; ++i) {
                keysBuilder.add(in.readObject());
                values[i] = in.readObject();
            }

            map = verifyNotNull(createInstance(keysBuilder.build(), values));
        } else {
            throw new IllegalStateException("already deserialized");
        }
    }

    abstract @NonNull T createInstance(@NonNull ImmutableList<Object> keys, @NonNull Object[] values);

    @java.io.Serial
    final Object readResolve() {
        return verifyNotNull(map);
    }
}
