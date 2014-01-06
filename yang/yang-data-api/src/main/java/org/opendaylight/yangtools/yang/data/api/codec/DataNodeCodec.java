package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.Node;

public interface DataNodeCodec<I> extends Codec<I, Node<?>> {

    @Override
    public Node<?> deserialize(I input);

    @Override
    public I serialize(Node<?> input);
}
