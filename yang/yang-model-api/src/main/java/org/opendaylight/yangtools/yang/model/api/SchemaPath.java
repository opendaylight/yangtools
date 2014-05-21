/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Arrays;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 *
 * Represents unique path to the every node inside the module.
 *
 */
public class SchemaPath {
    /**
     * List of QName instances which represents complete path to the node.
     */
    private final ImmutableList<QName> path;

    /**
     * Boolean value which represents type of schema path (relative or
     * absolute).
     */
    private final Boolean absolute;

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
     * @deprecated Use {@link #create(Iterable, boolean)} instead.
     */
    @Deprecated
    public SchemaPath(final List<QName> path, final boolean absolute) {
        this(ImmutableList.copyOf(path), absolute, null);
    }

    /**
     * Returns the complete path to schema node.
     *
     * @return list of <code>QName</code> instances which represents complete
     *         path to schema node
     *
     * @deprecated Use {@link #getPathFromRoot()} instead.
     */
    @Deprected
    public List<QName> getPath() {
        return path;
    }

    private SchemaPath(final ImmutableList<QName> path, final boolean absolute, final Void dummy) {
        this.path = Preconditions.checkNotNull(path);
        this.absolute = absolute;
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
        return new SchemaPath(ImmutableList.copyOf(path), absolute, null);
    }

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative path
     * @return A new child path
     */
    public SchemaPath createChild(final Iterable<QName> relative) {
        return create(Iterables.concat(path, relative), absolute);
    }

    /**
     * Create a child path based on concatenation of this path and a relative path.
     *
     * @param relative Relative SchemaPath
     * @return A new child path
     */
    public SchemaPath createChild(final SchemaPath relative) {
        Preconditions.checkArgument(!relative.isAbsolute(), "Child creation requires relative path");
        return create(Iterables.concat(path, relative.path), absolute);
    }

    /**
     * Create a child path based on concatenation of this path and additional
     * path elements.
     *
     * @param elements Relative SchemaPath elements
     * @return A new child path
     */
    public SchemaPath createChild(final QName... elements) {
        if (elements.length != 0) {
            return create(Iterables.concat(path, Arrays.asList(elements)), absolute);
        } else {
            return this;
        }
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
        return path;
    }

    /**
     * Returns the list of nodes which need to be traversed to get from this
     * node to the starting point (root for absolute SchemaPaths).
     *
     * @return list of <code>qname</code> instances which represents
     *         path from the schema node towards the root.
     */
    public Iterable<QName> getPathTowardsRoot() {
        return path.reverse();
    }

    /**
     * Describes whether schema path is|isn't absolute.
     *
     * @return boolean value which is <code>true</code> if schema path is
     *         absolute.
     */
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + absolute.hashCode();
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
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
        SchemaPath other = (SchemaPath) obj;
        if (absolute != other.absolute) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SchemaPath [path=");
        builder.append(path);
        builder.append(", absolute=");
        builder.append(absolute);
        builder.append("]");
        return builder.toString();
    }
}
