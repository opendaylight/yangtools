/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;

public abstract class LeafRefPath implements Immutable {
    /**
     * An absolute LeafRefPath.
     */
    private static final class AbsoluteLeafRefPath extends LeafRefPath {
        private AbsoluteLeafRefPath(final LeafRefPath parent, final QNameWithPredicate qname) {
            super(parent, qname);
        }

        @Override
        public boolean isAbsolute() {
            return true;
        }

        @Override
        protected LeafRefPath createInstance(final LeafRefPath newParent, final QNameWithPredicate newQname) {
            return new AbsoluteLeafRefPath(newParent, newQname);
        }
    }

    /**
     * A relative LeafRefPath.
     */
    private static final class RelativeLeafRefPath extends LeafRefPath {
        private RelativeLeafRefPath(final LeafRefPath parent, final QNameWithPredicate qname) {
            super(parent, qname);
        }

        @Override
        public boolean isAbsolute() {
            return false;
        }

        @Override
        protected LeafRefPath createInstance(final LeafRefPath newParent, final QNameWithPredicate newQname) {
            return new RelativeLeafRefPath(newParent, newQname);
        }
    }

    private static final VarHandle LEGACYPATH;

    static {
        try {
            LEGACYPATH = MethodHandles.lookup().findVarHandle(LeafRefPath.class, "legacyPath", ImmutableList.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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
     * Cached legacy path, filled-in when {@link #getPathFromRoot()} is invoked.
     */
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile ImmutableList<QNameWithPredicate> legacyPath;

    protected LeafRefPath(final LeafRefPath parent, final QNameWithPredicate qname) {
        this.parent = parent;
        this.qname = qname;

        int hc = Objects.hashCode(parent);
        if (qname != null) {
            hc = hc * 31 + qname.hashCode();
        }

        hash = hc;
    }

    /**
     * Constructs new instance of this class with the concrete path.
     *
     * @param path list of QNameWithPredicate instances which specifies exact path to the module node
     * @param absolute boolean value which specifies if the path is absolute or relative
     * @return A LeafRefPath instance.
     */
    public static LeafRefPath create(final Iterable<QNameWithPredicate> path, final boolean absolute) {
        final LeafRefPath parent = absolute ? ROOT : SAME;
        return parent.createChild(path);
    }

    /**
     * Constructs new instance of this class with the concrete path.
     *
     * @param absolute boolean value which specifies if the path is absolute or relative
     * @param path one or more QNameWithPredicate instances which specifies exact path to the module node
     * @return A LeafRefPath instance.
     */
    public static LeafRefPath create(final boolean absolute, final QNameWithPredicate... path) {
        return create(Arrays.asList(path), absolute);
    }

    /**
     * Create a new instance.
     *
     * @param newParent Parent LeafRefPath
     * @param newQname next path element
     * @return A new LeafRefPath instance
     */
    protected abstract LeafRefPath createInstance(LeafRefPath newParent, QNameWithPredicate newQname);

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative path
     * @return A new child path
     */
    public LeafRefPath createChild(final Iterable<QNameWithPredicate> relative) {
        if (Iterables.isEmpty(relative)) {
            return this;
        }

        LeafRefPath newParent = this;
        for (QNameWithPredicate relativeQname : relative) {
            newParent = newParent.createInstance(newParent, relativeQname);
        }

        return newParent;
    }

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative LeafRefPath
     * @return A new child path
     */
    public LeafRefPath createChild(final LeafRefPath relative) {
        checkArgument(!relative.isAbsolute(), "Child creation requires relative path");

        LeafRefPath newParent = this;
        for (QNameWithPredicate relativeQname : relative.getPathFromRoot()) {
            newParent = newParent.createInstance(newParent, relativeQname);
        }

        return newParent;
    }

    /**
     * Create a child path based on concatenation of this path and additional path elements.
     *
     * @param elements Relative LeafRefPath elements
     * @return A new child path
     */
    public LeafRefPath createChild(final QNameWithPredicate... elements) {
        return createChild(Arrays.asList(elements));
    }

    /**
     * Returns the list of nodes which need to be traversed to get from the starting point (root for absolute
     * LeafRefPaths) to the node represented by this object.
     *
     * @return list of {@code qname} instances which represents path from the root to the schema node.
     */
    public Iterable<QNameWithPredicate> getPathFromRoot() {
        final var local = (ImmutableList<QNameWithPredicate>) LEGACYPATH.getAcquire(this);
        return local != null ? local : loadLegacyPath();
    }

    private ImmutableList<QNameWithPredicate> loadLegacyPath() {
        final var ret = ImmutableList.copyOf(getPathTowardsRoot()).reverse();
        final var witness = (ImmutableList<QNameWithPredicate>) LEGACYPATH.compareAndExchangeRelease(this, null, ret);
        return witness != null ? witness : ret;
    }

    /**
     * Returns the list of nodes which need to be traversed to get from this node to the starting point (root
     * for absolute LeafRefPaths).
     *
     * @return list of {@code qname} instances which represents path from the schema node towards the root.
     */
    public Iterable<QNameWithPredicate> getPathTowardsRoot() {
        return () -> new Iterator<>() {
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
     * @return boolean value which is {@code true} if schema path is  absolute.
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
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final LeafRefPath other = (LeafRefPath) obj;
        return Objects.equals(qname, other.qname) && Objects.equals(parent, other.parent);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(isAbsolute() ? "Absolute path:" : "Relative path:");

        for (QNameWithPredicate qnameWithPredicate : getPathFromRoot()) {
            sb.append('/').append(qnameWithPredicate);
        }

        return sb.toString();
    }
}
