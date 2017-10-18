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
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

public final class QNameModule implements Comparable<QNameModule>, Immutable, Serializable {
    private static final Interner<QNameModule> INTERNER = Interners.newWeakInterner();
    private static final long serialVersionUID = 3L;

    private final URI namespace;

    //Nullable
    private final Revision revision;

    private transient int hash;

    private QNameModule(final URI namespace, final Revision revision) {
        this.namespace = requireNonNull(namespace);
        this.revision = revision;
    }

    /**
     * Return an interned reference to a equivalent QNameModule.
     *
     * @return Interned reference, or this object if it was interned.
     */
    public QNameModule intern() {
        return INTERNER.intern(this);
    }

    /**
     * Create a new QName module instance with specified namespace/revision.
     *
     * @param namespace Module namespace
     * @param revision Module revision
     * @return A new, potentially shared, QNameModule instance
     */
    public static QNameModule create(final URI namespace, final Optional<Revision> revision) {
        return new QNameModule(namespace, revision.orElse(null));
    }

    /**
     * Create a new QName module instance with specified namespace and norevision.
     *
     * @param namespace Module namespace
     * @return A new, potentially shared, QNameModule instance
     */
    public static QNameModule create(final URI namespace) {
        return new QNameModule(namespace, null);
    }

    /**
     * Create a new QName module instance with specified namespace/revision.
     *
     * @param namespace Module namespace
     * @param revision Module revision
     * @return A new, potentially shared, QNameModule instance
     */
    public static QNameModule create(final URI namespace, @Nullable final Revision revision) {
        return new QNameModule(namespace, revision);
    }

    /**
     * Returns the namespace of the module which is specified as argument of
     * YANG Module <b><font color="#00FF00">namespace</font></b> keyword.
     *
     * @return URI format of the namespace of the module
     */
    public URI getNamespace() {
        return namespace;
    }

    /**
     * Returns the revision date for the module.
     *
     * @return date of the module revision which is specified as argument of
     *         YANG Module <b><font color="#339900">revison</font></b> keyword
     */
    public Optional<Revision> getRevision() {
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

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(namespace, revision);
        }
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QNameModule)) {
            return false;
        }
        final QNameModule other = (QNameModule) obj;
        return Objects.equals(revision, other.revision) && Objects.equals(namespace, other.namespace);
    }

    /**
     * Returns a namespace in form defined by section 5.6.4. of
     * <a href=https://tools.ietf.org/html/rfc6020">RFC6020</a>, for example
     * {@code http://example.acme.com/system?revision=2008-04-01}.
     *
     * @return Namespace in form defined by section 5.6.4. of RFC6020.
     * @throws URISyntaxException on incorrect namespace definition
     *
     */
    URI getRevisionNamespace() throws URISyntaxException {
        final String query = revision == null ? "" : "revision=" + revision.toString();
        return new URI(namespace.getScheme(), namespace.getUserInfo(), namespace.getHost(), namespace.getPort(),
            namespace.getPath(), query, namespace.getFragment());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(QNameModule.class).omitNullValues().add("ns", getNamespace())
            .add("rev", revision).toString();
    }
}
