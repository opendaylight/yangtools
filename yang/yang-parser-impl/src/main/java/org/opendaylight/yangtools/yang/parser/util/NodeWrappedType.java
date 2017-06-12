/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.NodeImpl;

/**
 * @deprecated This class is not used anywhere and is scheduled for removal with TopologicalSort.
 */
@Deprecated
public final class NodeWrappedType extends NodeImpl {
    /**
     * The payload which is saved inside Node
     */
    private final Object wrappedType;

    /**
     * Create new instance of class <code>NodeWrappedType</code>.
     *
     * @param wrappedType
     *            object with payload data
     */
    public NodeWrappedType(final Object wrappedType) {
        this.wrappedType = wrappedType;
    }

    /**
     * Gets payload from class
     *
     * @return object with <code>wrappedType</code>
     */
    public Object getWrappedType() {
        return wrappedType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeWrappedType)) {
            return false;
        }
        NodeWrappedType nodeWrappedType = (NodeWrappedType) o;
        if (!wrappedType.equals(nodeWrappedType.wrappedType)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return wrappedType.hashCode();
    }

    @Override
    public String toString() {
        return "NodeWrappedType{" + "wrappedType=" + wrappedType + '}';
    }

}
