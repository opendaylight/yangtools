/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;

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
        public AbsoluteSchemaPath createChild(final QName element) {
            return new AbsoluteSchemaPath(this, requireNonNull(element));
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
        public RelativeSchemaPath createChild(final QName element) {
            return new RelativeSchemaPath(this, requireNonNull(element));
        }
    }

    /**
     * Shared instance of the conceptual root schema node.
     */
    public static final @NonNull SchemaPath ROOT = new AbsoluteSchemaPath(null, null);

    /**
     * Shared instance of the "same" relative schema node.
     */
    public static final @NonNull SchemaPath SAME = new RelativeSchemaPath(null, null);

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

    SchemaPath(final SchemaPath parent, final QName qname) {
        this.parent = parent;
        this.qname = qname;

        int tmp = Objects.hashCode(parent);
        if (qname != null) {
            tmp = tmp * 31 + qname.hashCode();
        }

        hash = tmp;
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
    public static @NonNull SchemaPath create(final Iterable<QName> path, final boolean absolute) {
        return (absolute ? ROOT : SAME).createChild(path);
    }

    /**
     * Constructs new instance of this class with the concrete path.
     *
     * @param absolute
     *            boolean value which specifies if the path is absolute or
     *            relative
     * @param element
     *            a single QName which specifies exact path to the
     *            module node
     *
     * @return A SchemaPath instance.
     */
    public static @NonNull SchemaPath create(final boolean absolute, final QName element) {
        return (absolute ? ROOT : SAME).createChild(element);
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
    public static @NonNull SchemaPath create(final boolean absolute, final QName... path) {
        return create(Arrays.asList(path), absolute);
    }

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative path
     * @return A new child path
     */
    public @NonNull SchemaPath createChild(final Iterable<QName> relative) {
        if (Iterables.isEmpty(relative)) {
            return this;
        }

        SchemaPath parentPath = this;
        for (QName item : relative) {
            parentPath = parentPath.createChild(item);
        }

        return parentPath;
    }

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative SchemaPath
     * @return A new child path
     */
    public @NonNull SchemaPath createChild(final SchemaPath relative) {
        checkArgument(!relative.isAbsolute(), "Child creation requires relative path");
        return createChild(relative.getPathFromRoot());
    }

    /**
     * Create a child path based on concatenation of this path and an additional path element.
     *
     * @param element Relative SchemaPath elements
     * @return A new child path
     */
    public abstract @NonNull SchemaPath createChild(QName element);

    /**
     * Create a child path based on concatenation of this path and additional
     * path elements.
     *
     * @param elements Relative SchemaPath elements
     * @return A new child path
     */
    public @NonNull SchemaPath createChild(final QName... elements) {
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
    public List<QName> getPathFromRoot() {
        if (qname == null) {
            return ImmutableList.of();
        }
        return parent == null ? ImmutableList.of(qname) : new PathFromRoot(this);
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
                }

                throw new NoSuchElementException("No more elements available");
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

    /**
     * Return this path as a {@link SchemaNodeIdentifier}.
     *
     * @return A SchemaNodeIdentifier.
     * @throws IllegalStateException if this path is empty
     */
    public final SchemaNodeIdentifier asSchemaNodeIdentifier() {
        checkState(qname != null, "Cannot convert empty %s", this);
        final List<QName> path = getPathFromRoot();
        return isAbsolute() ? Absolute.of(path) : Descendant.of(path);
    }

    /**
     * Return this path as an {@link Absolute} SchemaNodeIdentifier.
     *
     * @return An SchemaNodeIdentifier.
     * @throws IllegalStateException if this path is empty or is not absolute.
     */
    public final Absolute asAbsolute() {
        final SchemaNodeIdentifier ret = asSchemaNodeIdentifier();
        if (ret instanceof Absolute) {
            return (Absolute) ret;
        }
        throw new IllegalStateException("Path " + this + " is relative");
    }

    /**
     * Return this path as an {@link Descendant} SchemaNodeIdentifier.
     *
     * @return An SchemaNodeIdentifier.
     * @throws IllegalStateException if this path is empty or is not relative.
     */
    public final Descendant asDescendant() {
        final SchemaNodeIdentifier ret = asSchemaNodeIdentifier();
        if (ret instanceof Descendant) {
            return (Descendant) ret;
        }
        throw new IllegalStateException("Path " + this + " is absolute");
    }

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
