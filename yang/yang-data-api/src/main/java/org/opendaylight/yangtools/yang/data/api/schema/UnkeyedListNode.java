package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;


public interface UnkeyedListNode extends
    DataContainerChild<NodeIdentifier, Iterable<UnkeyedListEntryNode>>,
    OrderedNodeContainer<UnkeyedListEntryNode> {

}
