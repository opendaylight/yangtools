/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

final class DSIv1 implements Externalizable {
    private static final long serialVersionUID = 1L;

    private QName qname;

    @SuppressWarnings("checkstyle:redundantModifier")
    public DSIv1() {
        // For Externalizable
    }

    DSIv1(final @NonNull QName qname) {
        this.qname = requireNonNull(qname);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        qname.writeTo(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException {
        qname = QName.readFrom(in);
    }

    private Object readResolve() {
        return DatastoreIdentifier.create(qname);
    }
}
