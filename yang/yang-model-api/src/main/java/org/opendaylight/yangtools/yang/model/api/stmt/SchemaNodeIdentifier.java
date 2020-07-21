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
 * Represents unique path to the every schema node inside the schema node identifier namespace. This concept is defined
 * in <a href="https://tools.ietf.org/html/rfc7950#section-6.5">RFC7950</a>.
 */
public abstract class SchemaNodeIdentifier implements Immutable {
    /**
     * An absolute schema node identifier.
     */
    public static final class Absolute extends SchemaNodeIdentifier {
        private static final Interner<Absolute> INTERNER = Interners.newWeakInterner();

        Absolute(final QName qname) {
            super(qname);
        }

        Absolute(final Collection<QName> qnames) {
            super(qnames);
        }

        public static @NonNull Absolute of(final QName nodeIdentifier) {
            return new Absolute(nodeIdentifier);
        }

        public static @NonNull Absolute of(final QName... nodeIdentifiers) {
            return new Absolute(Arrays.asList(nodeIdentifiers));
        }

        public static @NonNull Absolute of(final Collection<QName> nodeIdentifiers) {
            return new Absolute(nodeIdentifiers);
        }

        public @NonNull Absolute intern() {
            return INTERNER.intern(this);
        }

        @Override
        SchemaPath implicitSchemaPathParent() {
            return SchemaPath.ROOT;
        }
    }

    /**
     * A descendant schema node identifier.
     */
    public static final class Descendant extends SchemaNodeIdentifier {
        Descendant(final QName qname) {
            super(qname);
        }

        Descendant(final Collection<QName> qnames) {
            super(qnames);
        }

        public static @NonNull Descendant of(final QName nodeIdentifier) {
            return new Descendant(nodeIdentifier);
        }

        public static @NonNull Descendant of(final QName... nodeIdentifiers) {
            return new Descendant(Arrays.asList(nodeIdentifiers));
        }

        public static @NonNull Descendant of(final Collection<QName> nodeIdentifiers) {
            return new Descendant(nodeIdentifiers);
        }

        @Override
        SchemaPath implicitSchemaPathParent() {
            return SchemaPath.SAME;
        }
    }

    private static final AtomicReferenceFieldUpdater<SchemaNodeIdentifier, SchemaPath> SCHEMAPATH_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(SchemaNodeIdentifier.class, SchemaPath.class, "schemaPath");

    private final @NonNull Object qnames;

    // Cached SchemaPath.
    private volatile SchemaPath schemaPath;
    // Cached hashCode
    private volatile int hash;

    SchemaNodeIdentifier(final QName qname) {
        this.qnames = requireNonNull(qname);
    }

    SchemaNodeIdentifier(final Collection<QName> qnames) {
        final ImmutableList<QName> tmp = ImmutableList.copyOf(qnames);
        checkArgument(!tmp.isEmpty(), "SchemaNodeIdentifier has to have at least one node identifier");
        this.qnames = tmp.size() == 1 ? tmp.get(0) : tmp;
    }

    public @NonNull List<QName> getNodeIdentifiers() {
        return qnames instanceof QName ? ImmutableList.of((QName) qnames) : (ImmutableList<QName>) qnames;
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

    private SchemaPath loadSchemaPath() {
        final SchemaPath newPath = implicitSchemaPathParent().createChild(getNodeIdentifiers());
        return SCHEMAPATH_UPDATER.compareAndSet(this, null, newPath) ? newPath : schemaPath;
    }

    abstract SchemaPath implicitSchemaPathParent();

    @Override
    public final int hashCode() {
        final int local;
        return (local = hash) != 0 ? local : (hash = qnames.hashCode());
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return qnames.equals(((SchemaNodeIdentifier) obj).qnames);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("qnames", toStringQNames()).toString();
    }

    private List<?> toStringQNames() {
        final List<QName> ids = getNodeIdentifiers();
        return ids.size() < 2 ? ids : simplifyQNames(ids);
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
