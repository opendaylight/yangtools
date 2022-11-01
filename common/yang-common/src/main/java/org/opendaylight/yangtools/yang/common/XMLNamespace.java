/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A simple type capture of {@code namespace} statement's argument according to
 * <a href="https://tools.ietf.org/html/rfc6020#section-7.1.3">RFC6020</a>.
 */
public final class XMLNamespace implements Comparable<XMLNamespace>, Immutable, Serializable {
    private static final Interner<XMLNamespace> INTERNER = Interners.newWeakInterner();
    @Serial
    private static final long serialVersionUID = 1L;

    private final String namespace;

    private XMLNamespace(final String namespace) {
        this.namespace = requireNonNull(namespace);
    }

    // FIXME: add documentation
    public static @NonNull XMLNamespace of(final String namespace) {
        try {
            // Validation only
            new URI(namespace);
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
    public @NonNull XMLNamespace intern() {
        return INTERNER.intern(this);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final XMLNamespace o) {
        return namespace.compareTo(o.namespace);
    }

    @Override
    public int hashCode() {
        return namespace.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
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
}
