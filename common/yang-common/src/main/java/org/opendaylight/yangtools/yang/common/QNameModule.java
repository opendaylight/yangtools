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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import javax.management.modelmbean.XMLParseException;
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
    @java.io.Serial
    private static final long serialVersionUID = 3L;

    private final @NonNull XMLNamespace namespace;
    private final @NonNull RevisionUnion revUnion;

    private transient int hash = 0;

    private QNameModule(final XMLNamespace namespace, final RevisionUnion revUnion) {
        this.namespace = requireNonNull(namespace);
        this.revUnion = requireNonNull(revUnion);
    }

    /**
     * Create a new QName module instance with specified {@link XMLNamespace} and no revision.
     *
     * @param namespace Module namespace
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull QNameModule of(final XMLNamespace namespace) {
        return of(namespace, RevisionUnion.none());
    }

    /**
     * Create a new QName module instance with specified {@link XMLNamespace} and {@link RevisionUnion}.
     *
     * @param namespace Module namespace
     * @param revUnion Module revision
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull QNameModule of(final XMLNamespace namespace, final RevisionUnion revUnion) {
        return new QNameModule(namespace, revUnion);
    }

    /**
     * Create a new QName module instance with specified {@link XMLNamespace} and {@link Revision}.
     *
     * @param namespace Module namespace
     * @param revision Module revision
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull QNameModule of(final XMLNamespace namespace, final Revision revision) {
        return new QNameModule(namespace, revision);
    }

    /**
     * Create a new QName module instance with specified namespace string and no revision.
     *
     * @param namespace Module namespace
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull QNameModule of(final String namespace) {
        return of(XMLNamespace.of(namespace));
    }

    /**
     * Create a new QName module instance with specified namespace string and {@link RevisionUnion} string.
     *
     * @param namespace Module namespace
     * @param unionString Module revision string or an empty string
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull QNameModule of(final String namespace, final String unionString) {
        return of(XMLNamespace.of(namespace), RevisionUnion.of(unionString));
    }

    /**
     * Create a new QName module instance with specified {@link XMLParseException} and an optional {@link Revision}.
     *
     * @param namespace Module namespace
     * @param revision Module revision
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull QNameModule ofRevision(final XMLNamespace namespace, final @Nullable Revision revision) {
        return of(namespace, revision != null ? revision : RevisionUnion.none());
    }

    /**
     * Create a new QName module instance with specified {@link XMLNamespace} and an optional {@link Revision}.
     *
     * @param namespace Module namespace
     * @param revision Module revision
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if {@code namespace} is {@code null}
     */
    public static @NonNull QNameModule ofRevision(final String namespace, final @Nullable String revision) {
        return of(XMLNamespace.of(namespace), revision != null ? Revision.of(revision) : RevisionUnion.none());
    }

    /**
     * Create a new QName module instance with specified namespace/revision.
     *
     * @param namespace Module namespace
     * @param revision Module revision
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is {@code null}
     * @deprecated Use {@link #ofRevision(XMLNamespace, Revision)} instead
     */
    @Deprecated(since = "12.0.1", forRemoval = true)
    public static @NonNull QNameModule create(final XMLNamespace namespace, final Optional<Revision> revision) {
        return ofRevision(namespace, revision.orElse(null));
    }

    /**
     * Create a new QName module instance with specified namespace and no revision.
     *
     * @param namespace Module namespace
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if {@code namespace} is null
     * @deprecated Use {@link #of(XMLNamespace)} instead
     */
    @Deprecated(since = "12.0.1", forRemoval = true)
    public static @NonNull QNameModule create(final XMLNamespace namespace) {
        return of(namespace);
    }

    /**
     * Create a new QName module instance with specified namespace/revision.
     *
     * @param namespace Module namespace
     * @param revision Module revision
     * @return A new, potentially shared, QNameModule instance
     * @throws NullPointerException if any argument is {@code null}
     * @deprecated Use {@link #ofRevision(XMLNamespace, Revision)} instead
     */
    @Deprecated(since = "12.0.1", forRemoval = true)
    public static @NonNull QNameModule create(final XMLNamespace namespace, final @Nullable Revision revision) {
        return ofRevision(namespace, revision);
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
        return new QNameModule(XMLNamespace.readFrom(in), RevisionUnion.readFrom(in));
    }

    /**
     * Returns the namespace of the module which is specified as argument of YANG Module {@code namespace} keyword.
     *
     * @return XMLNamespace of the namespace of the module
     */
    public @NonNull XMLNamespace namespace() {
        return namespace;
    }

    /**
     * Returns the namespace of the module which is specified as argument of YANG Module {@code namespace} keyword.
     *
     * @return XMLNamespace of the namespace of the module
     * @deprecated Use {@link #namespace()} instead.
     */
    @Deprecated(since = "12.0.1", forRemoval = true)
    public @NonNull XMLNamespace getNamespace() {
        return namespace();
    }

    /**
     * Returns the revision date for the module.
     *
     * @return date of the module revision which is specified as argument of YANG Module {@code revision} keyword
     */
    public @NonNull RevisionUnion revisionUnion() {
        return revUnion;
    }

    /**
     * Returns the revision date for the module.
     *
     * @return date of the module revision which is specified as argument of YANG Module {@code revision} keyword
     */
    public @Nullable Revision revision() {
        return revUnion.revision();
    }

    /**
     * Returns the revision date for the module.
     *
     * @return date of the module revision which is specified as argument of YANG Module {@code revision} keyword
     */
    public @NonNull Optional<Revision> findRevision() {
        return revUnion.findRevision();
    }

    /**
     * Returns the revision date for the module.
     *
     * @return date of the module revision which is specified as argument of YANG Module {@code revision} keyword
     * @deprecated Use {@link #findRevision()} or {@link #revision()} instead.
     */
    @Deprecated(since = "12.0.1", forRemoval = true)
    public @NonNull Optional<Revision> getRevision() {
        return findRevision();
    }

    /**
     * Return an interned reference to a equivalent QNameModule.
     *
     * @return Interned reference, or this object if it was interned.
     */
    public @NonNull QNameModule intern() {
        return INTERNER.intern(this);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final QNameModule o) {
        int cmp;
        return (cmp = namespace.compareTo(o.namespace)) != 0 ? cmp : revUnion.compareTo(o.revUnion);
    }

    /**
     * Returns a QNameModule with the same namespace, but with no revision. If this QNameModule does not have
     * a revision, this object is returned.
     *
     * @return a QNameModule with the same namespace, but with no revision.
     */
    public @NonNull QNameModule withoutRevision() {
        return revUnion instanceof NotRevision ? this : of(namespace);
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(namespace.toString());
        out.writeUTF(revUnion.unionString());
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(namespace, revUnion);
        }
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof QNameModule other && revUnion.equals(other.revUnion)
            && namespace.equals(other.namespace);
    }

    @Override
    public @NonNull String toString() {
        return MoreObjects.toStringHelper(QNameModule.class).omitNullValues()
            .add("ns", namespace)
            .add("rev", revUnion.revision())
            .toString();
    }

    @java.io.Serial
    Object writeReplace() {
        return new NSv1(this);
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
