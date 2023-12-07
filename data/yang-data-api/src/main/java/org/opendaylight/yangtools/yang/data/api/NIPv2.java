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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map.Entry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

/**
 * Externalizable proxy for {@link NodeIdentifierWithPredicates}.
 */
final class NIPv2 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private NodeIdentifierWithPredicates nip;

    @SuppressWarnings("checkstyle:redundantModifier")
    public NIPv2() {
        // For Externalizable
    }

    NIPv2(final NodeIdentifierWithPredicates nip) {
        this.nip = requireNonNull(nip);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        nip.getNodeType().writeTo(out);

        out.writeInt(nip.size());
        for (Entry<QName, Object> entry : nip.entrySet()) {
            entry.getKey().writeTo(out);
            out.writeObject(entry.getValue());
        }
    }

    @Override
    @SuppressFBWarnings(value = "SE_PREVENT_EXT_OBJ_OVERWRITE",
        justification = "https://github.com/spotbugs/spotbugs/issues/2750")
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final QName qname = QName.readFrom(in);
        final int size = in.readInt();
        nip = switch (size) {
            case 0 -> NodeIdentifierWithPredicates.of(qname);
            case 1 -> NodeIdentifierWithPredicates.of(qname, QName.readFrom(in), in.readObject());
            default -> {
                final var keys = ImmutableMap.<QName, Object>builderWithExpectedSize(size);
                for (int i = 0; i < size; ++i) {
                    keys.put(QName.readFrom(in), in.readObject());
                }
                yield NodeIdentifierWithPredicates.of(qname, keys.build());
            }
        };
    }

    @java.io.Serial
    private Object readResolve() {
        return nip;
    }
}
