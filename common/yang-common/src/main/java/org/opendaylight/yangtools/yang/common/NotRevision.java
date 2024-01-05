/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.DataInput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An empty alternative to {@link Revision}. This contract is exactly the same as the {@code type string} block from
 * this fragment from {@code ietf-yang-library}:
 * <pre>{@code
 *   type union {
 *     type revision-identifier;
 *     type string {
 *       length "0";
 *     }
 *   }
 * }</pre>
 */
@NonNullByDefault
public final class NotRevision implements RevisionUnion {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final NotRevision INSTANCE = new NotRevision();

    private NotRevision() {
        // Hidden on purpose
    }

    public static NotRevision of() {
        return INSTANCE;
    }

    public static NotRevision readFrom(final DataInput in) throws IOException {
        final var str = in.readUTF();
        if (!str.isEmpty()) {
            throw new IOException("Unexpected value '" + str + "'");
        }
        return of();
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
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj;
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
