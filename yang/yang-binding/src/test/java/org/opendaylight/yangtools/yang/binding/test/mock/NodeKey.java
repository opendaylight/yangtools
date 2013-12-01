package org.opendaylight.yangtools.yang.binding.test.mock;

import org.opendaylight.yangtools.yang.binding.Identifier;

public class NodeKey implements //
        Identifier<Node> {

    private final int id;

    public NodeKey(int id) {
        super();
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodeKey other = (NodeKey) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
