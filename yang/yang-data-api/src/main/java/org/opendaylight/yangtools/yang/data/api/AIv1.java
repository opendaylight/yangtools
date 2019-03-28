/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;

/**
 * Externalizable proxy for {@link AugmentationIdentifier}.
 */
final class AIv1 implements Externalizable {
    private static final long serialVersionUID = 1L;

    private AugmentationIdentifier ai;

    @SuppressWarnings("checkstyle:redundantModifier")
    public AIv1() {
        // For Externalizable
    }

    AIv1(final AugmentationIdentifier ai) {
        this.ai = requireNonNull(ai);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(ai.getPossibleChildNames().size());
        for (QName qname : ai.getPossibleChildNames()) {
            qname.writeTo(out);
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException {
        final int count = in.readInt();
        final QName[] qnames = new QName[count];
        for (int i = 0; i < count; ++i) {
            qnames[i] = QName.readFrom(in);
        }
        ai = new AugmentationIdentifier(ImmutableSet.copyOf(qnames));
    }

    private Object readResolve() {
        return ai;
    }
}
