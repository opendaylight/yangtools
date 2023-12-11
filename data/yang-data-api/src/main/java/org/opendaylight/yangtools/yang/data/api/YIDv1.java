/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Externalizable proxy for {@link YangInstanceIdentifier}.
 */
final class YIDv1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private YangInstanceIdentifier yid;

    @SuppressWarnings("checkstyle:redundantModifier")
    public YIDv1() {
        // For Externalizable
    }

    YIDv1(final YangInstanceIdentifier yid) {
        this.yid = requireNonNull(yid);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        final var args = yid.getPathArguments();
        out.writeInt(args.size());
        for (PathArgument arg : args) {
            // Unfortunately PathArgument is an interface and we do not have control over all its implementations,
            // hence we did not bother with making them WritableObjects. This works reasonably well, though.
            out.writeObject(arg);
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final int size = in.readInt();
        final var builder = ImmutableList.<PathArgument>builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            builder.add((PathArgument) in.readObject());
        }
        yid = YangInstanceIdentifier.of(builder.build());
    }

    @java.io.Serial
    private Object readResolve() {
        return yid;
    }
}
