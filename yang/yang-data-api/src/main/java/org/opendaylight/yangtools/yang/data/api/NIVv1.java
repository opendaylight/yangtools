/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static java.util.Objects.requireNonNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;

/**
 * Externalizable proxy for {@link NodeWithValue}.
 */
final class NIVv1 implements Externalizable {
    private static final long serialVersionUID = 1L;

    private NodeWithValue<?> niv;

    @SuppressWarnings("checkstyle:redundantModifier")
    public NIVv1() {
        // For Externalizable
    }

    NIVv1(final NodeWithValue<?> niv) {
        this.niv = requireNonNull(niv);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        niv.getNodeType().writeTo(out);
        out.writeObject(niv.getValue());
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        niv = new NodeWithValue<>(QName.readFrom(in), in.readObject());
    }

    private Object readResolve() {
        return niv;
    }
}
