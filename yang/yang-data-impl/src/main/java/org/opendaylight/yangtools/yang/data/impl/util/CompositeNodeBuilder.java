package org.opendaylight.yangtools.yang.data.impl.util;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.ImmutableCompositeNode;

public interface CompositeNodeBuilder<P extends CompositeNode> extends NodeBuilder<P,CompositeNodeBuilder<P>> {

    CompositeNodeBuilder<P> addLeaf(QName leafName,Object leafValue);
    CompositeNodeBuilder<P> add(Node<?> node);
    CompositeNodeBuilder<P> addAll(Iterable<? extends Node<?>> nodes);
    CompositeNodeBuilder<P> addLeaf(String leafLocalName, String leafValue);
}
