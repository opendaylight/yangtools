package org.opendaylight.yangtools.yang.data.impl.codec;

import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;

public interface DataContainerCodec<T extends DataContainer> extends  DomCodec<T> {


    @Override
    public ValueWithQName<T> deserialize(Node<?> input);

    @Override
    public CompositeNode serialize(ValueWithQName<T> input);
}
