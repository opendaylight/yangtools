package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder.ImmutableLeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder.ImmutableMapEntryNode;

public class ImmutableNodes {




    public static final ImmutableMapNodeBuilder mapNodeBuilder() {
        return ImmutableMapNodeBuilder.create();
    }

    public static final ImmutableMapNodeBuilder mapNodeBuilder(QName name) {
        return ImmutableMapNodeBuilder.create().withNodeIdentifier(new NodeIdentifier(name));
    }


    public static final <T> ImmutableLeafNode<T> leafNode(QName name,T value) {
        return new ImmutableLeafNode<T>(new NodeIdentifier(name), value);
    }

    public static ImmutableMapEntryNodeBuilder mapEntryBuilder(QName nodeName,QName keyName,Object keyValue) {
        return ImmutableMapEntryNodeBuilder.create()
                .withNodeIdentifier(new NodeIdentifierWithPredicates(nodeName, keyName,keyValue))
                .withChild(leafNode(keyName, keyValue));
    }

    public static ImmutableMapEntryNode mapEntry(QName nodeName,QName keyName,Object keyValue) {
        return mapEntryBuilder(nodeName, keyName, keyValue).build();
    }

    public static ImmutableMapEntryNodeBuilder mapEntryBuilder() {
        return ImmutableMapEntryNodeBuilder.create();
    }

    public static NormalizedNode<?, ?> containerNode(QName name) {
        return ImmutableContainerNodeBuilder.create().withNodeIdentifier(new NodeIdentifier(name)).build();
    }

}
