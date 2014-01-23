package org.opendaylight.yangtools.yang.data.impl.codec;

import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;

public interface IdentifierCodec<I extends Identifier<?>> extends DomCodec<I> {

    @Override
    public ValueWithQName<I> deserialize(Node<?> input);

    @Override
    public CompositeNode serialize(ValueWithQName<I> input);
}
