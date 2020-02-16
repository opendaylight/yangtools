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
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Represents unique path to the every schema node inside the schema node identifier namespace. This concept is defined
 * in <a ref="https://tools.ietf.org/html/rfc7950#section-6.5">RFC7950</a>.
 */
public abstract class SchemaNodeIdentifier implements Immutable {
    /**
     * An absolute schema node identifier.
     */
    public static final class Absolute extends SchemaNodeIdentifier {
        private Absolute(final SchemaNodeIdentifier parent, final QName qname) {
            super(parent, qname);
        }

        @Override
        public boolean isAbsolute() {
            return true;
        }

        @Override
        public Absolute createChild(final QName element) {
            return new Absolute(this, requireNonNull(element));
        }
    }

    /**
     * A descendant schema node identifier.
     */
    public static final class Descendant extends SchemaNodeIdentifier {
        private Descendant(final SchemaNodeIdentifier parent, final QName qname) {
            super(parent, qname);
        }

        @Override
        public boolean isAbsolute() {
            return false;
        }

        @Override
        public Descendant createChild(final QName element) {
            return new Descendant(this, requireNonNull(element));
        }
    }

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<SchemaNodeIdentifier, ImmutableList> LEGACYPATH_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(SchemaNodeIdentifier.class, ImmutableList.class, "legacyPath");
    private static final AtomicReferenceFieldUpdater<SchemaNodeIdentifier, SchemaPath> SCHEMAPATH_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(SchemaNodeIdentifier.class, SchemaPath.class, "schemaPath");
    /**
     * Shared instance of the conceptual root schema node.
     */
    public static final Absolute ROOT = new Absolute(null, null);

    /**
     * Shared instance of the "same" relative schema node.
     */
    public static final Descendant SAME = new Descendant(null, null);

    /**
     * Parent path.
     */
    private final SchemaNodeIdentifier parent;

    /**
     * This component.
     */
    private final QName qname;

    /**
     * Cached hash code. We can use this since we are immutable.
     */
    private final int hash;

    /**
     * Cached legacy path, filled-in when {@link #getPath()} or {@link #getPathTowardsRoot()}
     * is invoked.
     */
    private volatile ImmutableList<QName> legacyPath;

    /**
     * Cached SchemaPath.
     */
    private volatile SchemaPath schemaPath;

    SchemaNodeIdentifier(final SchemaNodeIdentifier parent, final QName qname) {
        this.parent = parent;
        this.qname = qname;

        int tmp = Objects.hashCode(parent);
        if (qname != null) {
            tmp = tmp * 31 + qname.hashCode();
        }

        hash = tmp;
    }

    private ImmutableList<QName> getLegacyPath() {
        ImmutableList<QName> ret = legacyPath;
        if (ret == null) {
            ret = ImmutableList.copyOf(getPathTowardsRoot()).reverse();
            LEGACYPATH_UPDATER.lazySet(this, ret);
        }

        return ret;
    }

    /**
     * Constructs new instance of this class with the concrete path.
     *
     * @param path
     *            list of QName instances which specifies exact path to the
     *            module node
     * @param absolute
     *            boolean value which specifies if the path is absolute or
     *            relative
     *
     * @return A SchemaNodeIdentifier instance.
     */
    public static SchemaNodeIdentifier create(final Iterable<QName> path, final boolean absolute) {
        final SchemaNodeIdentifier parent = absolute ? ROOT : SAME;
        return parent.createChild(path);
    }

    /**
     * Constructs new instance of this class with the concrete path.
     *
     * @param absolute
     *            boolean value which specifies if the path is absolute or
     *            relative
     * @param path
     *            one or more QName instances which specifies exact path to the
     *            module node
     *
     * @return A SchemaPath instance.
     */
    public static SchemaNodeIdentifier create(final boolean absolute, final QName... path) {
        return create(Arrays.asList(path), absolute);
    }

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative path
     * @return A new child path
     */
    public SchemaNodeIdentifier createChild(final Iterable<QName> relative) {
        if (Iterables.isEmpty(relative)) {
            return this;
        }

        SchemaNodeIdentifier parentNode = this;
        for (QName item : relative) {
            parentNode = parentNode.createChild(item);
        }

        return parentNode;
    }

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative SchemaPath
     * @return A new child path
     */
    public SchemaNodeIdentifier createChild(final SchemaNodeIdentifier relative) {
        checkArgument(!relative.isAbsolute(), "Child creation requires relative path");
        return createChild(relative.getPathFromRoot());
    }

    /**
     * Create a child path based on concatenation of this path and an additional path element.
     *
     * @param element Next SchemaPath element
     * @return A new child path
     */
    public abstract SchemaNodeIdentifier createChild(QName element);

    /**
     * Create a child path based on concatenation of this path and additional
     * path elements.
     *
     * @param elements Relative SchemaPath elements
     * @return A new child path
     */
    public SchemaNodeIdentifier createChild(final QName... elements) {
        return createChild(Arrays.asList(elements));
    }

    /**
     * Returns the list of nodes which need to be traversed to get from the
     * starting point (root for absolute SchemaPaths) to the node represented
     * by this object.
     *
     * @return list of <code>qname</code> instances which represents
     *         path from the root to the schema node.
     */
    public Iterable<QName> getPathFromRoot() {
        return getLegacyPath();
    }

    /**
     * Returns the list of nodes which need to be traversed to get from this
     * node to the starting point (root for absolute SchemaPaths).
     *
     * @return list of <code>qname</code> instances which represents
     *         path from the schema node towards the root.
     */
    public Iterable<QName> getPathTowardsRoot() {
        return () -> new UnmodifiableIterator<>() {
            private SchemaNodeIdentifier current = SchemaNodeIdentifier.this;

            @Override
            public boolean hasNext() {
                return current.parent != null;
            }

            @Override
            public QName next() {
                if (current.parent != null) {
                    final QName ret = current.qname;
                    current = current.parent;
                    return ret;
                } else {
                    throw new NoSuchElementException("No more elements available");
                }
            }
        };
    }

    /**
     * Returns the immediate parent SchemaPath.
     *
     * @return Parent path, null if this SchemaPath is already toplevel.
     */
    public SchemaNodeIdentifier getParent() {
        return parent;
    }

    /**
     * Get the last component of this path.
     *
     * @return The last component of this path.
     */
    public final QName getLastComponent() {
        return qname;
    }

    private SchemaPath createSchemaPath() {
        final SchemaPath newPath;
        if (parent == null) {
            final SchemaPath parentPath = isAbsolute() ? SchemaPath.ROOT : SchemaPath.SAME;
            newPath = qname == null ? parentPath : parentPath.createChild(qname);
        } else {
            newPath = parent.asSchemaPath().createChild(qname);
        }

        return SCHEMAPATH_UPDATER.compareAndSet(this, null, newPath) ? newPath : schemaPath;
    }

    /**
     * Create the {@link SchemaPath} equivalent of this identifier.
     *
     * @return SchemaPath equivalent.
     */
    public final SchemaPath asSchemaPath() {
        final SchemaPath ret = schemaPath;
        return ret != null ? ret : createSchemaPath();
    }

    /**
     * Describes whether schema node identifier is|isn't absolute.
     *
     * @return boolean value which is <code>true</code> if schema path is
     *         absolute.
     */
    public abstract boolean isAbsolute();

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchemaNodeIdentifier other = (SchemaNodeIdentifier) obj;
        return Objects.equals(qname, other.qname) && Objects.equals(parent, other.parent);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("path", getPathFromRoot());
    }
}
