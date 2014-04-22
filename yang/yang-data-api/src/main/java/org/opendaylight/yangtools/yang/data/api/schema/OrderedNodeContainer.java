package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;

public interface OrderedNodeContainer<V extends NormalizedNode<?, ?>> extends MixinNode, NormalizedNode<NodeIdentifier, Iterable<V>> {

    V getChild(int position);

    int getSize();

}
