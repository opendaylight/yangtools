/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.yang.types;

import org.opendaylight.yangtools.util.TopologicalSort.NodeImpl;

final class NodeWrappedType extends NodeImpl {
    /**
     * The payload which is saved inside Node.
     */
    private final Object wrappedType;

    /**
     * Create new instance of class <code>NodeWrappedType</code>.
     *
     * @param wrappedType object with payload data
     */
    NodeWrappedType(final Object wrappedType) {
        this.wrappedType = wrappedType;
    }

    /**
     * Gets payload from class.
     *
     * @return object with <code>wrappedType</code>
     */
    Object getWrappedType() {
        return wrappedType;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NodeWrappedType)) {
            return false;
        }
        NodeWrappedType nodeWrappedType = (NodeWrappedType) obj;
        return wrappedType.equals(nodeWrappedType.wrappedType);
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
