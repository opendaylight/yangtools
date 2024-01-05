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
 * Externalizable proxy for {@link QNameModule}.
 */
final class NSv1 implements Externalizable {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private QNameModule namespace;

    @SuppressWarnings("checkstyle:redundantModifier")
    public NSv1() {
        // For Externalizable
    }

    NSv1(final QNameModule qname) {
        namespace = requireNonNull(qname);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        namespace.writeTo(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException {
        namespace = QNameModule.readFrom(in);
    }

    @java.io.Serial
    Object readResolve() {
        return verifyNotNull(namespace);
    }
}
