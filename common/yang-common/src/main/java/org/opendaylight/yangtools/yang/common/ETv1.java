/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;

/**
 * Externalizable proxy for {@link ErrorTag}.
 */
final class ETv1 implements Externalizable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String elementBody;

    @SuppressWarnings("checkstyle:redundantModifier")
    public ETv1() {
        // visible for Externalizable
    }

    ETv1(final ErrorTag tag) {
        elementBody = tag.elementBody();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(elementBody);
    }

    @Override
    @SuppressFBWarnings(value = "SE_PREVENT_EXT_OBJ_OVERWRITE",
        justification = "https://github.com/spotbugs/spotbugs/issues/2750")
    public void readExternal(final ObjectInput in) throws IOException {
        elementBody = in.readUTF();
    }

    @Serial
    Object readResolve() {
        return new ErrorTag(elementBody);
    }
}
