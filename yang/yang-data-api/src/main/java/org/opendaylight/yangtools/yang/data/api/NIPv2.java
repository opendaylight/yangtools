/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map.Entry;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

/**
 * Externalizable proxy for {@link NodeIdentifierWithPredicates}.
 */
final class NIPv2 implements Externalizable {
    private static final long serialVersionUID = 1L;

    private NodeIdentifierWithPredicates nip;

    @SuppressWarnings("checkstyle:redundantModifier")
    public NIPv2() {
        // For Externalizable
    }

    NIPv2(final NodeIdentifierWithPredicates nid) {
        this.nip = requireNonNull(nid);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        nip.getNodeType().writeTo(out);

        out.writeInt(nip.size());
        final Set<Entry<QName, Object>> entries = nip.entrySet();
        for (Entry<QName, Object> entry : entries) {
            entry.getKey().writeTo(out);
            out.writeObject(entry.getValue());
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final QName qname = QName.readFrom(in);
        final int size = in.readInt();
        switch (size) {
            case 0:
                nip = NodeIdentifierWithPredicates.of(qname);
                break;
            case 1:
                nip = NodeIdentifierWithPredicates.of(qname, QName.readFrom(in), in.readObject());
                break;
            default:
                final Builder<QName, Object> keys = ImmutableMap.builderWithExpectedSize(size);
                for (int i = 0; i < size; ++i) {
                    keys.put(QName.readFrom(in), in.readObject());
                }
                nip = NodeIdentifierWithPredicates.of(qname, keys.build());
        }
    }

    private Object readResolve() {
        return nip;
    }
}
