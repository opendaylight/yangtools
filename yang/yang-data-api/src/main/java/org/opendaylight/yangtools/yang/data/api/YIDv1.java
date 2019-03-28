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
import com.google.common.collect.ImmutableList.Builder;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Externalizable proxy for {@link YangInstanceIdentifier}.
 */
final class YIDv1 implements Externalizable {
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
        final List<PathArgument> args = yid.getPathArguments();
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
        final Builder<PathArgument> builder = ImmutableList.builderWithExpectedSize(size);
        for (int i = 0; i < size; ++i) {
            builder.add((PathArgument) in.readObject());
        }
        yid = YangInstanceIdentifier.create(builder.build());
    }

    private Object readResolve() {
        return yid;
    }
}
