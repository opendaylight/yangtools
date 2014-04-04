package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import java.util.List;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public interface NormalizedNodeContainerBuilder<K extends PathArgument,CK extends PathArgument,CV extends NormalizedNode<? extends CK, ?>,P extends NormalizedNode<K, ?>>
extends NormalizedNodeBuilder<K,List<CV>,P>{

    @Override
    NormalizedNodeContainerBuilder<K,CK,CV,P> withNodeIdentifier(K nodeIdentifier);

    @Override
    NormalizedNodeContainerBuilder<K,CK,CV,P> withValue(List<CV> value);

    NormalizedNodeContainerBuilder<K,CK,CV,P> addChild(CV child);
    NormalizedNodeContainerBuilder<K,CK,CV,P> removeChild(CK key);
}
