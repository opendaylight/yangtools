/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Represents unique path to every schema node inside the schema node identifier namespace. This concept is defined
 * in <a href="https://tools.ietf.org/html/rfc7950#section-6.5">RFC7950</a>.
 */
public abstract class SchemaNodeIdentifier implements Immutable {
    /**
     * An absolute schema node identifier.
     */
    public abstract static class Absolute extends SchemaNodeIdentifier {
        private static final Interner<Absolute> INTERNER = Interners.newWeakInterner();

        Absolute() {
            // Hidden on purpose
        }

        /**
         * Create an absolute schema node identifier composed of a single node identifier.
         *
         * @param nodeIdentifier Single node identifier
         * @return An absolute schema node identifier
         * @throws NullPointerException if {@code nodeIdentifier} is null
         */
        public static @NonNull Absolute of(final QName nodeIdentifier) {
            return new AbsoluteSingle(nodeIdentifier);
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
            return of(Arrays.asList(nodeIdentifiers));
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
            final ImmutableList<QName> qnames = checkQNames(nodeIdentifiers);
            return qnames.size() == 1 ? of(qnames.get(0)) : new AbsoluteMultiple(qnames);
        }

        /**
         * Return an interned reference to an equivalent object.
         *
         * @return An interned reference, or this object if it was previously interned.
         */
        public final @NonNull Absolute intern() {
            return INTERNER.intern(this);
        }

        @Override
        final SchemaPath implicitSchemaPathParent() {
            return SchemaPath.ROOT;
        }

        @Override
        final String className() {
            return "Absolute";
        }
    }

    /**
     * A descendant schema node identifier.
     */
    public abstract static class Descendant extends SchemaNodeIdentifier {
        Descendant() {
            // Hidden on purpose
        }

        /**
         * Create a descendant schema node identifier composed of a single node identifier.
         *
         * @param nodeIdentifier Single node identifier
         * @return A descendant schema node identifier
         * @throws NullPointerException if {@code nodeIdentifier} is null
         */
        public static @NonNull Descendant of(final QName nodeIdentifier) {
            return new DescendantSingle(nodeIdentifier);
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
            return of(Arrays.asList(nodeIdentifiers));
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
            final ImmutableList<QName> qnames = checkQNames(nodeIdentifiers);
            return qnames.size() == 1 ? of(qnames.get(0)) : new DescandantMultiple(qnames);
        }

        @Override
        final SchemaPath implicitSchemaPathParent() {
            return SchemaPath.SAME;
        }

        @Override
        final String className() {
            return "Descendant";
        }
    }

    private static final class AbsoluteSingle extends Absolute {
        private final @NonNull QName qname;

        AbsoluteSingle(final QName qname) {
            this.qname = requireNonNull(qname);
        }

        @Override
        public ImmutableList<QName> getNodeIdentifiers() {
            return ImmutableList.of(qname);
        }

        @Override
        public QName firstNodeIdentifier() {
            return qname;
        }

        @Override
        public QName lastNodeIdentifier() {
            return qname;
        }

        @Override
        Object pathObject() {
            return qname;
        }
    }

    private static final class AbsoluteMultiple extends Absolute {
        private final @NonNull ImmutableList<QName> qnames;

        AbsoluteMultiple(final ImmutableList<QName> qnames) {
            this.qnames = requireNonNull(qnames);
        }

        @Override
        public ImmutableList<QName> getNodeIdentifiers() {
            return qnames;
        }

        @Override
        Object pathObject() {
            return qnames;
        }
    }

    private static final class DescendantSingle extends Descendant {
        private final @NonNull QName qname;

        DescendantSingle(final QName qname) {
            this.qname = requireNonNull(qname);
        }

        @Override
        public ImmutableList<QName> getNodeIdentifiers() {
            return ImmutableList.of(qname);
        }

        @Override
        public QName firstNodeIdentifier() {
            return qname;
        }

        @Override
        public QName lastNodeIdentifier() {
            return qname;
        }

        @Override
        Object pathObject() {
            return qname;
        }
    }

    private static final class DescandantMultiple extends Descendant {
        private final @NonNull ImmutableList<QName> qnames;

        DescandantMultiple(final ImmutableList<QName> qnames) {
            this.qnames = requireNonNull(qnames);
        }

        @Override
        public ImmutableList<QName> getNodeIdentifiers() {
            return qnames;
        }

        @Override
        Object pathObject() {
            return qnames;
        }
    }

    private static final AtomicReferenceFieldUpdater<SchemaNodeIdentifier, SchemaPath> SCHEMAPATH_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(SchemaNodeIdentifier.class, SchemaPath.class, "schemaPath");

    // Cached SchemaPath.
    private volatile SchemaPath schemaPath;
    // Cached hashCode
    private volatile int hash;

    SchemaNodeIdentifier() {
        // Hidden on purpose
    }

    /**
     * Return the non-empty sequence of node identifiers which constitute this schema node identifier.
     *
     * @return Non-empty sequence of node identifiers
     */
    public abstract @NonNull List<QName> getNodeIdentifiers();

    /**
     * Return the first node identifier. This method is equivalent to {@code getNodeIdentifiers().get(0)}, but is
     * potentially more efficient.
     *
     * @return The first node identifier
     */
    public @NonNull QName firstNodeIdentifier() {
        return getNodeIdentifiers().get(0);
    }

    /**
     * Return the last node identifier. This method is equivalent to {@code getNodeIdentifiers().get(size - 1)}, but
     * is potentially more efficient.
     *
     * @return The last node identifier
     */
    public @NonNull QName lastNodeIdentifier() {
        final List<QName> local = getNodeIdentifiers();
        return local.get(local.size() - 1);
    }

    /**
     * Create the {@link SchemaPath} equivalent of this identifier.
     *
     * @return SchemaPath equivalent.
     */
    public final @NonNull SchemaPath asSchemaPath() {
        final SchemaPath ret = schemaPath;
        return ret != null ? ret : loadSchemaPath();
    }

    @Override
    public final int hashCode() {
        final int local;
        return (local = hash) != 0 ? local : (hash = pathObject().hashCode());
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass()
                && pathObject().equals(((SchemaNodeIdentifier) obj).pathObject());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(className()).add("qnames", toStringQNames()).toString();
    }

    abstract @NonNull SchemaPath implicitSchemaPathParent();

    abstract @NonNull Object pathObject();

    abstract @NonNull String className();

    private @NonNull SchemaPath loadSchemaPath() {
        final SchemaPath newPath = implicitSchemaPathParent().createChild(getNodeIdentifiers());
        return SCHEMAPATH_UPDATER.compareAndSet(this, null, newPath) ? newPath : schemaPath;
    }

    private List<?> toStringQNames() {
        final List<QName> ids = getNodeIdentifiers();
        return ids.size() < 2 ? ids : simplifyQNames(ids);
    }

    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD",
            justification = "https://github.com/spotbugs/spotbugs/issues/811")
    private static ImmutableList<QName> checkQNames(final Collection<QName> qnames) {
        final ImmutableList<QName> ret = ImmutableList.copyOf(qnames);
        checkArgument(!ret.isEmpty(), "SchemaNodeIdentifier has to have at least one node identifier");
        return ret;
    }

    private static List<?> simplifyQNames(final List<QName> qnames) {
        final List<Object> ret = new ArrayList<>(qnames.size());

        QNameModule prev = null;
        for (QName qname : qnames) {
            final QNameModule module = qname.getModule();
            ret.add(module.equals(prev) ? qname.getLocalName() : qname);
            prev = module;
        }

        return ret;
    }
}
