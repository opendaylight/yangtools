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
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ImmutableOffsetMap.Ordered;
import org.opendaylight.yangtools.util.ImmutableOffsetMap.Unordered;

/**
 * Serialization proxy for {@link ImmutableOffsetMap}.
 *
 */
final class IOMv1 implements Externalizable {
    @Serial
    private static final long serialVersionUID = 1;

    private ImmutableOffsetMap<?, ?> map;

    @SuppressWarnings("checkstyle:RedundantModifier")
    public IOMv1() {
        // For Externalizable
    }

    IOMv1(final @NonNull ImmutableOffsetMap<?, ?> map) {
        this.map = requireNonNull(map);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        final var local = verifyNotNull(map);

        // FIXME: use out.writeBoolean(<expression on sealed class>) instead
        if (local instanceof Ordered) {
            out.writeBoolean(true);
        } else if (map instanceof Unordered) {
            out.writeBoolean(false);
        } else {
            throw new IOException("Unhandled implementation " + map.getClass());
        }

        out.writeInt(local.size());
        for (var e : local.entrySet()) {
            out.writeObject(e.getKey());
            out.writeObject(e.getValue());
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final boolean ordered = in.readBoolean();

        // TODO: optimize for size == 1? what can we gain?
        final int size = in.readInt();
        final var keysBuilder = ImmutableList.builderWithExpectedSize(size);
        final var values = new Object[size];
        for (int i = 0; i < size; ++i) {
            keysBuilder.add(in.readObject());
            values[i] = in.readObject();
        }

        final var keys = keysBuilder.build();
        map = ordered ? Ordered.ofSerialized(keys, values) : Unordered.ofSerialized(keys, values);
    }

    @Serial
    private Object readReplace() {
        return verifyNotNull(map);
    }
}
