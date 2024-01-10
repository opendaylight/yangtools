/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Represents unique path to every schema node inside the schema node identifier namespace. This concept is defined
 * in <a href="https://www.rfc-editor.org/rfc/rfc7950#section-6.5">RFC7950</a>.
 */
public abstract sealed class SchemaNodeIdentifier implements Immutable {
    /**
     * An absolute schema node identifier.
     */
    public static final class Absolute extends SchemaNodeIdentifier {
        private static final Interner<@NonNull Absolute> INTERNER = Interners.newWeakInterner();

        private Absolute(final ImmutableList<QName> qnames) {
            super(qnames);
        }

        private Absolute(final QName qname) {
            super(qname);
        }

        /**
         * Create an absolute schema node identifier composed of a single node identifier.
         *
         * @param nodeIdentifier Single node identifier
         * @return An absolute schema node identifier
         * @throws NullPointerException if {@code nodeIdentifier} is null
         */
        public static @NonNull Absolute of(final QName nodeIdentifier) {
            return new Absolute(nodeIdentifier);
        }

        /**
         * Create an absolute schema node identifier composed of multiple node identifiers.
         *
         * @param nodeIdentifiers Node identifiers
         * @return An absolute schema node identifier
         * @throws NullPointerException if {@code nodeIdentifiers} or any of its members is null
         * @throws IllegalArgumentException if {@code nodeIdentifiers} is empty
         */
        public static @NonNull Absolute of(final QName... nodeIdentifiers) {
            return of(ImmutableList.copyOf(nodeIdentifiers));
        }

        /**
         * Create an absolute schema node identifier composed of multiple node identifiers.
         *
         * @param nodeIdentifiers Node identifiers
         * @return An absolute schema node identifier
         * @throws NullPointerException if {@code nodeIdentifiers} or any of its members is null
         * @throws IllegalArgumentException if {@code nodeIdentifiers} is empty
         */
        public static @NonNull Absolute of(final Collection<QName> nodeIdentifiers) {
            final var qnames = ImmutableList.copyOf(nodeIdentifiers);
            return qnames.size() == 1 ? of(qnames.get(0)) : new Absolute(qnames);
        }

        /**
         * Return an interned reference to an equivalent object.
         *
         * @return An interned reference, or this object if it was previously interned.
         */
        public @NonNull Absolute intern() {
            return INTERNER.intern(this);
        }
    }

    /**
     * A descendant schema node identifier.
     */
    public static final class Descendant extends SchemaNodeIdentifier {
        private Descendant(final ImmutableList<QName> qnames) {
            super(qnames);
        }

        private Descendant(final QName qname) {
            super(qname);
        }

        /**
         * Create a descendant schema node identifier composed of a single node identifier.
         *
         * @param nodeIdentifier Single node identifier
         * @return A descendant schema node identifier
         * @throws NullPointerException if {@code nodeIdentifier} is null
         */
        public static @NonNull Descendant of(final QName nodeIdentifier) {
            return new Descendant(nodeIdentifier);
        }

        /**
         * Create a descendant schema node identifier composed of multiple node identifiers.
         *
         * @param nodeIdentifiers Node identifiers
         * @return A descendant schema node identifier
         * @throws NullPointerException if {@code nodeIdentifiers} or any of its members is null
         * @throws IllegalArgumentException if {@code nodeIdentifiers} is empty
         */
        public static @NonNull Descendant of(final QName... nodeIdentifiers) {
            return of(ImmutableList.copyOf(nodeIdentifiers));
        }

        /**
         * Create a descendant schema node identifier composed of multiple node identifiers.
         *
         * @param nodeIdentifiers Node identifiers
         * @return A descendant schema node identifier
         * @throws NullPointerException if {@code nodeIdentifiers} or any of its members is null
         * @throws IllegalArgumentException if {@code nodeIdentifiers} is empty
         */
        public static @NonNull Descendant of(final Collection<QName> nodeIdentifiers) {
            final var qnames = ImmutableList.copyOf(nodeIdentifiers);
            return qnames.size() == 1 ? of(qnames.get(0)) : new Descendant(qnames);
        }
    }

    private final @NonNull Object pathObj;

    // Cached hashCode
    private volatile int hash;

    private SchemaNodeIdentifier(final QName qname) {
        pathObj = requireNonNull(qname);
    }

    private SchemaNodeIdentifier(final ImmutableList<QName> qnames) {
        if (qnames.isEmpty()) {
            throw new IllegalArgumentException("SchemaNodeIdentifier has to have at least one node identifier");
        }
        pathObj = qnames;
    }

    /**
     * Return the non-empty sequence of node identifiers which constitute this schema node identifier.
     *
     * @return Non-empty sequence of node identifiers
     */
    public final @NonNull List<QName> getNodeIdentifiers() {
        return pathObj instanceof QName qname ? ImmutableList.of(qname) : coerceList();
    }

    /**
     * Return the first node identifier. This method is equivalent to {@code getNodeIdentifiers().get(0)}, but is
     * potentially more efficient.
     *
     * @return The first node identifier
     */
    public final @NonNull QName firstNodeIdentifier() {
        return pathObj instanceof QName qname ? qname : coerceList().get(0);
    }

    /**
     * Return the last node identifier. This method is equivalent to {@code getNodeIdentifiers().get(size - 1)}, but
     * is potentially more efficient.
     *
     * @return The last node identifier
     */
    public final @NonNull QName lastNodeIdentifier() {
        if (pathObj instanceof QName qname) {
            return qname;
        }
        final var list = coerceList();
        return list.get(list.size() - 1);
    }

    @Override
    public final int hashCode() {
        final int local;
        return (local = hash) != 0 ? local : (hash = pathObj.hashCode());
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass()
            && pathObj.equals(((SchemaNodeIdentifier) obj).pathObj);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this)
            .add("qnames", pathObj instanceof QName qname ? ImmutableList.of(qname) : simpleQNames())
            .toString();
    }

    private List<?> simpleQNames() {
        final var qnames = coerceList();
        final var ret = new ArrayList<>(qnames.size());

        QNameModule prev = null;
        for (var qname : qnames) {
            final var module = qname.getModule();
            ret.add(module.equals(prev) ? qname.getLocalName() : qname);
            prev = module;
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    private ImmutableList<QName> coerceList() {
        return (ImmutableList<QName>) pathObj;
    }
}
