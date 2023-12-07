/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;

/**
 * Externalizable proxy for {@link XMLNamespace}.
 */
final class XNv1 implements Externalizable {
    @Serial
    private static final long serialVersionUID = 1L;

    private XMLNamespace namespace;

    @SuppressWarnings("checkstyle:redundantModifier")
    public XNv1() {
        // For Externalizable
    }

    XNv1(final XMLNamespace namespace) {
        this.namespace = requireNonNull(namespace);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(namespace.toString());
    }

    @Override
    @SuppressFBWarnings(value = "SE_PREVENT_EXT_OBJ_OVERWRITE",
        justification = "https://github.com/spotbugs/spotbugs/issues/2750")
    public void readExternal(final ObjectInput in) throws IOException {
        try {
            namespace = XMLNamespace.of(in.readUTF());
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid namespace", e);
        }
    }

    @Serial
    Object readResolve() {
        return verifyNotNull(namespace);
    }
}
