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

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * A simple type capture of {@code namespace} statement's argument according to
 * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-7.1.3">RFC6020</a>.
 */
@NonNullByDefault
public final class XMLNamespace implements Comparable<XMLNamespace>, Immutable, Serializable, WritableObject {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final Interner<XMLNamespace> INTERNER = Interners.newWeakInterner();

    private final String namespace;

    private XMLNamespace(final String namespace) {
        this.namespace = requireNonNull(namespace);
    }

    // FIXME: add documentation
    public static XMLNamespace of(final String namespace) {
        try {
            // FIXME: we want this validation, can we get it without the object allocation?
            verifyNotNull(new URI(namespace));
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException("Namespace '" + namespace + "' is not a valid URI", e);
        }

        return new XMLNamespace(namespace);
    }

    /**
     * Return an interned reference to a equivalent XMLNamespace.
     *
     * @return Interned reference, or this object if it was interned.
     */
    public XMLNamespace intern() {
        return INTERNER.intern(this);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final XMLNamespace o) {
        return namespace.compareTo(o.namespace);
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(namespace);
    }

    public static XMLNamespace readFrom(final DataInput in) throws IOException {
        try {
            return of(in.readUTF());
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid namespace", e);
        }
    }

    @Override
    public int hashCode() {
        return namespace.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof XMLNamespace other && namespace.equals(other.namespace);
    }

    @Override
    public String toString() {
        return namespace;
    }

    @Serial
    Object writeReplace() {
        return new XNv1(this);
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
