/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.opendaylight.yangtools.concepts.Immutable;

public abstract class LeafRefPath implements Immutable {

    /**
     * An absolute LeafRefPath.
     */
    private static final class AbsoluteLeafRefPath extends LeafRefPath {
        private AbsoluteLeafRefPath(final LeafRefPath parent,
                final QNameWithPredicate qname) {
            super(parent, qname);
        }

        @Override
        public boolean isAbsolute() {
            return true;
        }

        @Override
        protected LeafRefPath createInstance(final LeafRefPath parent,
                final QNameWithPredicate qname) {
            return new AbsoluteLeafRefPath(parent, qname);
        }
    }

    /**
     * A relative LeafRefPath.
     */
    private static final class RelativeLeafRefPath extends LeafRefPath {
        private RelativeLeafRefPath(final LeafRefPath parent,
                final QNameWithPredicate qname) {
            super(parent, qname);
        }

        @Override
        public boolean isAbsolute() {
            return false;
        }

        @Override
        protected LeafRefPath createInstance(final LeafRefPath parent,
                final QNameWithPredicate qname) {
            return new RelativeLeafRefPath(parent, qname);
        }
    }

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<LeafRefPath, ImmutableList> LEGACYPATH_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(LeafRefPath.class, ImmutableList.class, "legacyPath");

    /**
     * Shared instance of the conceptual root schema node.
     */
    public static final LeafRefPath ROOT = new AbsoluteLeafRefPath(null, null);

    /**
     * Shared instance of the "same" relative schema node.
     */
    public static final LeafRefPath SAME = new RelativeLeafRefPath(null, null);

    /**
     * Parent path.
     */
    private final LeafRefPath parent;

    /**
     * This component.
     */
    private final QNameWithPredicate qname;

    /**
     * Cached hash code. We can use this since we are immutable.
     */
    private final int hash;

    /**
     * Cached legacy path, filled-in when {@link #getPath()} or
     * {@link #getPathTowardsRoot()} is invoked.
     */
    private volatile ImmutableList<QNameWithPredicate> legacyPath;

    private ImmutableList<QNameWithPredicate> getLegacyPath() {
        ImmutableList<QNameWithPredicate> ret = legacyPath;
        if (ret == null) {
            ret = ImmutableList.copyOf(getPathTowardsRoot()).reverse();
            LEGACYPATH_UPDATER.lazySet(this, ret);
        }

        return ret;
    }

    protected LeafRefPath(final LeafRefPath parent, final QNameWithPredicate qname) {
        this.parent = parent;
        this.qname = qname;

        int h = Objects.hashCode(parent);
        if (qname != null) {
            h = h * 31 + qname.hashCode();
        }

        hash = h;
    }

    /**
     * Constructs new instance of this class with the concrete path.
     *
     * @param path
     *            list of QNameWithPredicate instances which specifies exact
     *            path to the module node
     * @param absolute
     *            boolean value which specifies if the path is absolute or
     *            relative
     *
     * @return A LeafRefPath instance.
     */
    public static LeafRefPath create(final Iterable<QNameWithPredicate> path,
            final boolean absolute) {
        final LeafRefPath parent = absolute ? ROOT : SAME;
        return parent.createChild(path);
    }

    /**
     * Constructs new instance of this class with the concrete path.
     *
     * @param absolute
     *            boolean value which specifies if the path is absolute or
     *            relative
     * @param path
     *            one or more QNameWithPredicate instances which specifies exact
     *            path to the module node
     *
     * @return A LeafRefPath instance.
     */
    public static LeafRefPath create(final boolean absolute,
            final QNameWithPredicate... path) {
        return create(Arrays.asList(path), absolute);
    }

    /**
     * Create a new instance.
     *
     * @param parent
     *            Parent LeafRefPath
     * @param qname
     *            next path element
     * @return A new LeafRefPath instance
     */
    protected abstract LeafRefPath createInstance(LeafRefPath parent,
            QNameWithPredicate qname);

    /**
     * Create a child path based on concatenation of this path and a relative
     * path.
     *
     * @param relative
     *            Relative path
     * @return A new child path
     */
    public LeafRefPath createChild(final Iterable<QNameWithPredicate> relative) {
        if (Iterables.isEmpty(relative)) {
            return this;
        }

        LeafRefPath parent = this;
        for (QNameWithPredicate qname : relative) {
            parent = parent.createInstance(parent, qname);
        }

        return parent;
    }

    /**
     * Create a child path based on concatenation of this path and a relative
     * path.
     *
     * @param relative
     *            Relative LeafRefPath
     * @return A new child path
     */
    public LeafRefPath createChild(final LeafRefPath relative) {
        checkArgument(!relative.isAbsolute(), "Child creation requires relative path");

        LeafRefPath parent = this;
        for (QNameWithPredicate qname : relative.getPathFromRoot()) {
            parent = parent.createInstance(parent, qname);
        }

        return parent;
    }

    /**
     * Create a child path based on concatenation of this path and additional
     * path elements.
     *
     * @param elements
     *            Relative LeafRefPath elements
     * @return A new child path
     */
    public LeafRefPath createChild(final QNameWithPredicate... elements) {
        return createChild(Arrays.asList(elements));
    }

    /**
     * Returns the list of nodes which need to be traversed to get from the
     * starting point (root for absolute LeafRefPaths) to the node represented
     * by this object.
     *
     * @return list of <code>qname</code> instances which represents path from
     *         the root to the schema node.
     */
    public Iterable<QNameWithPredicate> getPathFromRoot() {
        return getLegacyPath();
    }

    /**
     * Returns the list of nodes which need to be traversed to get from this
     * node to the starting point (root for absolute LeafRefPaths).
     *
     * @return list of <code>qname</code> instances which represents path from
     *         the schema node towards the root.
     */
    public Iterable<QNameWithPredicate> getPathTowardsRoot() {
        return () -> new Iterator<QNameWithPredicate>() {
            private LeafRefPath current = LeafRefPath.this;

            @Override
            public boolean hasNext() {
                return current.parent != null;
            }

            @Override
            public QNameWithPredicate next() {
                if (current.parent == null) {
                    throw new NoSuchElementException("No more elements available");
                }

                final QNameWithPredicate ret = current.qname;
                current = current.parent;
                return ret;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Component removal not supported");
            }
        };
    }

    /**
     * Returns the immediate parent LeafRefPath.
     *
     * @return Parent path, null if this LeafRefPath is already toplevel.
     */
    public LeafRefPath getParent() {
        return parent;
    }

    /**
     * Get the last component of this path.
     *
     * @return The last component of this path.
     */
    public final QNameWithPredicate getLastComponent() {
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
        final LeafRefPath other = (LeafRefPath) obj;
        return Objects.equals(qname, other.qname) && Objects.equals(parent, other.parent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        Iterable<QNameWithPredicate> pathFromRoot = this.getPathFromRoot();

        sb.append(isAbsolute() ? "Absolute path:" : "Relative path:");

        for (QNameWithPredicate qName : pathFromRoot) {
            sb.append('/').append(qName);
        }

        return sb.toString();

    }

    // @Override
    // public final String toString() {
    // return addToStringAttributes(Objects.toStringHelper(this)).toString();
    // }
    //
    // protected ToStringHelper addToStringAttributes(final ToStringHelper
    // toStringHelper) {
    // return toStringHelper.add("path", getPathFromRoot());
    // }

}
