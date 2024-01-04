/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

// Empty alternative to Revision
@NonNullByDefault
final class NotRevision implements RevisionUnion {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    static final NotRevision INSTANCE = new NotRevision();

    private NotRevision() {
        // Hidden on purpose
    }

    @Override
    public @Nullable Revision revision() {
        return null;
    }

    @Override
    public String unionString() {
        return "";
    }

    @Override
    public String toString() {
        return unionString();
    }

    @java.io.Serial
    Object writeReplace() {
        return new RUv1("");
    }

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        Revision.throwNSE();
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        Revision.throwNSE();
    }

    @java.io.Serial
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        Revision.throwNSE();
    }
}
