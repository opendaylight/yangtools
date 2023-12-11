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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * Externalizable proxy for {@link NodeIdentifier}.
 */
final class NIv1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private NodeIdentifier nid;

    @SuppressWarnings("checkstyle:redundantModifier")
    public NIv1() {
        // For Externalizable
    }

    NIv1(final NodeIdentifier nid) {
        this.nid = requireNonNull(nid);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        nid.getNodeType().writeTo(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException {
        nid = new NodeIdentifier(QName.readFrom(in));
    }

    @java.io.Serial
    private Object readResolve() {
        return nid;
    }
}
