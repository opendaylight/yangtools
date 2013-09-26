package org.opendaylight.yangtools.sal.binding.yang.types;

import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.NodeImpl;

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
    NodeWrappedType(Object wrappedType) {
        this.wrappedType = wrappedType;
    }

    /**
     * Gets payload from class
     * 
     * @return object with <code>wrappedType</code>
     */
    Object getWrappedType() {
        return wrappedType;
    }

    @Override
    public boolean equals(Object o) {
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
