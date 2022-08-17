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
import java.io.Serial;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

/**
 * Externalizable proxy for {@link NodeIdentifierWithPredicates}.
 *
 * @deprecated Since 4.0.0 in favor of {@link NIPv2}.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
final class NIPv1 implements Externalizable {
    @Serial
    private static final long serialVersionUID = 1L;

    private NodeIdentifierWithPredicates nip;

    @SuppressWarnings("checkstyle:redundantModifier")
    public NIPv1() {
        // For Externalizable
    }

    NIPv1(final NodeIdentifierWithPredicates nid) {
        nip = requireNonNull(nid);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        nip.getNodeType().writeTo(out);
        out.writeObject(nip.asMap());
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final QName qname = QName.readFrom(in);
        nip = NodeIdentifierWithPredicates.of(qname, (Map<QName, Object>) in.readObject());
    }

    @Serial
    private Object readResolve() {
        return nip;
    }
}
