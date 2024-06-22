/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import com.google.common.collect.ImmutableList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNull;

final class ORv1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private ImmutableList<@NonNull ExactDataObjectStep<?>> steps;

    @SuppressWarnings("redundantModifier")
    public ORv1() {
        // For Externalizable
    }

    ORv1(final DataObjectReference<?> source) {
        steps = source.steps();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(steps.size());
        for (var step : steps) {
            out.writeObject(step);
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final int size = in.readInt();
        final var builder = ImmutableList.<ExactDataObjectStep<?>>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            builder.add((ExactDataObjectStep<?>) in.readObject());
        }
        steps = builder.build();
    }

    @java.io.Serial
    Object readResolve() throws ObjectStreamException {
        return DataObjectReference.unsafeOf(steps);
    }
}
