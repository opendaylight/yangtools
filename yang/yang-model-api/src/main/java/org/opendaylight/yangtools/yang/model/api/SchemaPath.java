/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Represents unique path to the every node inside the module.
 */
public abstract class SchemaPath implements Immutable {

    /**
     * An absolute SchemaPath.
     */
    private static final class AbsoluteSchemaPath extends SchemaPath {
        private AbsoluteSchemaPath(final SchemaPath parent, final QName qname) {
            super(parent, qname);
        }

        @Override
        public boolean isAbsolute() {
            return true;
        }

        @Override
        protected SchemaPath createInstance(final SchemaPath parent, final QName qname) {
            return new AbsoluteSchemaPath(parent, requireNonNull(qname));
        }
    }

    /**
     * A relative SchemaPath.
     */
    private static final class RelativeSchemaPath extends SchemaPath {
        private RelativeSchemaPath(final SchemaPath parent, final QName qname) {
            super(parent, qname);
        }

        @Override
        public boolean isAbsolute() {
            return false;
        }

        @Override
        protected SchemaPath createInstance(final SchemaPath parent, final QName qname) {
            return new RelativeSchemaPath(parent, requireNonNull(qname));
        }
    }

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<SchemaPath, ImmutableList> LEGACYPATH_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(SchemaPath.class, ImmutableList.class, "legacyPath");

    /**
     * Shared instance of the conceptual root schema node.
     */
    public static final SchemaPath ROOT = new AbsoluteSchemaPath(null, null);

    /**
     * Shared instance of the "same" relative schema node.
     */
    public static final SchemaPath SAME = new RelativeSchemaPath(null, null);

    /**
     * Parent path.
     */
    private final SchemaPath parent;

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

    SchemaPath(final SchemaPath parent, final QName qname) {
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
            final List<QName> tmp = new ArrayList<>();
            for (QName qname : getPathTowardsRoot()) {
                tmp.add(qname);
            }
            ret = ImmutableList.copyOf(Lists.reverse(tmp));
            LEGACYPATH_UPDATER.lazySet(this, ret);
        }

        return ret;
    }

    /**
     * Returns the complete path to schema node.
     *
     * @return list of <code>QName</code> instances which represents complete
     *         path to schema node
     *
     * @deprecated Use {@link #getPathFromRoot()} instead.
     */
    @Deprecated
    public List<QName> getPath() {
        return getLegacyPath();
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
     * @return A SchemaPath instance.
     */
    public static SchemaPath create(final Iterable<QName> path, final boolean absolute) {
        final SchemaPath parent = absolute ? ROOT : SAME;
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
    public static SchemaPath create(final boolean absolute, final QName... path) {
        return create(Arrays.asList(path), absolute);
    }

    /**
     * Create a new instance.
     *
     * @param parent Parent SchemaPath
     * @param qname next path element
     * @return A new SchemaPath instance
     */
    protected abstract SchemaPath createInstance(SchemaPath parent, QName qname);

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative path
     * @return A new child path
     */
    public SchemaPath createChild(final Iterable<QName> relative) {
        if (Iterables.isEmpty(relative)) {
            return this;
        }

        SchemaPath parentPath = this;
        for (QName qname : relative) {
            parentPath = parentPath.createInstance(parentPath, qname);
        }

        return parentPath;
    }

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative SchemaPath
     * @return A new child path
     */
    public SchemaPath createChild(final SchemaPath relative) {
        checkArgument(!relative.isAbsolute(), "Child creation requires relative path");

        SchemaPath parentPath = this;
        for (QName qname : relative.getPathFromRoot()) {
            parentPath = parentPath.createInstance(parentPath, qname);
        }

        return parentPath;
    }

    /**
     * Create a child path based on concatenation of this path and additional
     * path elements.
     *
     * @param elements Relative SchemaPath elements
     * @return A new child path
     */
    public SchemaPath createChild(final QName... elements) {
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
        return () -> new UnmodifiableIterator<QName>() {
            private SchemaPath current = SchemaPath.this;

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
    public SchemaPath getParent() {
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

    /**
     * Describes whether schema path is|isn't absolute.
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
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchemaPath other = (SchemaPath) obj;
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
