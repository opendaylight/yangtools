/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Externalizable proxy for {@link UnqualifiedQName}.
 */
final class UQNv1 implements Externalizable {
    private static final long serialVersionUID = 1L;

    private UnqualifiedQName qname;

    @SuppressWarnings("checkstyle:redundantModifier")
    public UQNv1() {
        // For Externalizable
    }

    UQNv1(final UnqualifiedQName qname) {
        this.qname = requireNonNull(qname);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        qname.writeTo(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException {
        qname = UnqualifiedQName.readFrom(in);
    }

    Object readResolve() {
        return verifyNotNull(qname);
    }
}
