package org.opendaylight.yangtools.yang.data.impl.codec;

import org.opendaylight.yangtools.yang.binding.BindingCodec;
import org.opendaylight.yangtools.yang.data.api.Node;

public interface DomCodec<I> extends BindingCodec<Node<?>, ValueWithQName<I>>{


    @Override
    public Node<?> serialize(ValueWithQName<I> input);


    @Override
    public ValueWithQName<I> deserialize(Node<?> input);

}
