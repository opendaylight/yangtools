/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * A {@link XMLNamespace} bound to a particular model {@link Revision}. This is the primary way of identifying a YANG
 * module namespace within an effective model world. The reason for this is that we support coexistence of multiple
 * module revisions and hence cannot use plain module name or namespace to address them.
 */
public final class QNameModule implements Comparable<QNameModule>, Immutable, Serializable, Identifier, WritableObject {
    private static final Interner<QNameModule> INTERNER = Interners.newWeakInterner();
    @Serial
    private static final long serialVersionUID = 3L;

    private final @NonNull XMLNamespace namespace;
    private final @Nullable Revision revision;

    private transient int hash = 0;

    private QNameModule(final XMLNamespace namespace, final @Nullable Revision revision) {
        this.namespace = requireNonNull(namespace);
        this.revision = revision;
    }

    /**
     * Return an interned reference to a equivalent QNameModule.
     *
     * @return Interned reference, or this object if it was interned.
     */
    public @NonNull QNameModule intern() {
        return INTERNER.intern(this);
    }

    /**
     * Create a new QName module instance with specified namespace/revision.
     *
     * @param namespace Module namespace
     * @param revision Module revision
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is null
     */
    public static @NonNull QNameModule create(final XMLNamespace namespace, final Optional<Revision> revision) {
        return new QNameModule(namespace, revision.orElse(null));
    }

    /**
     * Create a new QName module instance with specified namespace and no revision.
     *
     * @param namespace Module namespace
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if {@code namespace} is null
     */
    public static @NonNull QNameModule create(final XMLNamespace namespace) {
        return new QNameModule(namespace, null);
    }

    /**
     * Create a new QName module instance with specified namespace/revision.
     *
     * @param namespace Module namespace
     * @param revision Module revision
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is null
     */
    public static @NonNull QNameModule create(final XMLNamespace namespace, final @Nullable Revision revision) {
        return new QNameModule(namespace, revision);
    }

    /**
     * Read a QNameModule from a DataInput. The format is expected to match the output format
     * of {@link #writeTo(DataOutput)}.
     *
     * @param in DataInput to read
     * @return A QNameModule instance
     * @throws IOException if I/O error occurs
     */
    public static @NonNull QNameModule readFrom(final DataInput in) throws IOException {
        final String namespace = in.readUTF();
        final String revision = in.readUTF();
        return new QNameModule(XMLNamespace.of(namespace), revision.isEmpty() ? null : Revision.of(revision));
    }

    /**
     * Returns the namespace of the module which is specified as argument of YANG Module {@code namespace} keyword.
     *
     * @return XMLNamespace of the namespace of the module
     */
    public @NonNull XMLNamespace getNamespace() {
        return namespace;
    }

    /**
     * Returns the revision date for the module.
     *
     * @return date of the module revision which is specified as argument of YANG Module {@code revision} keyword
     */
    public @NonNull Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final QNameModule o) {
        int cmp = namespace.compareTo(o.namespace);
        if (cmp != 0) {
            return cmp;
        }
        return Revision.compare(revision, o.revision);
    }

    /**
     * Returns a QNameModule with the same namespace, but with no revision. If this QNameModule does not have
     * a revision, this object is returned.
     *
     * @return a QNameModule with the same namespace, but with no revision.
     */
    public @NonNull QNameModule withoutRevision() {
        return revision == null ? this : new QNameModule(namespace, null);
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(namespace.toString());
        out.writeUTF(revision == null ? "" : revision.toString());
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(namespace, revision);
        }
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof QNameModule other
            && Objects.equals(revision, other.revision) && namespace.equals(other.namespace);
    }

    @Override
    public @NonNull String toString() {
        return MoreObjects.toStringHelper(QNameModule.class).omitNullValues()
            .add("ns", namespace)
            .add("rev", revision)
            .toString();
    }

    @Serial
    Object writeReplace() {
        return new NSv1(this);
    }
}
