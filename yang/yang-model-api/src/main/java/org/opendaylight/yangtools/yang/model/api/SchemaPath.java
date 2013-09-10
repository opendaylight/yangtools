/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * 
 * Represents unique path to the every node inside the module.
 * 
 */
public class SchemaPath {
    private final int hashBooleanTrue = 1231;
    private final int hashBooleanFalse = 1237;

    /**
     * List of QName instances which represents complete path to the node.
     */
    private final List<QName> path;

    /**
     * Boolean value which represents type of schema path (relative or
     * absolute).
     */
    private final boolean absolute;

    /**
     * Constructs new instance of this class with the concrete path.
     * 
     * @param path
     *            list of QName instances which specifies exact path to the
     *            module node
     * @param absolute
     *            boolean value which specifies if the path is absolute or
     *            relative
     */
    public SchemaPath(final List<QName> path, boolean absolute) {
        this.path = Collections.unmodifiableList(new ArrayList<QName>(path));
        this.absolute = absolute;
    }

    /**
     * Returns the complete path to schema node.
     * 
     * @return list of <code>QName</code> instances which represents complete
     *         path to schema node
     */
    public List<QName> getPath() {
        return path;
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
        result = prime * result + (absolute ? hashBooleanTrue : hashBooleanFalse);
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
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
