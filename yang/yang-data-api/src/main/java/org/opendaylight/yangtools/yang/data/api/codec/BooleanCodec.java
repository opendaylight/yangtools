package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;


public interface BooleanCodec<T> extends Codec<T,Boolean> {

    public T serialize(Boolean data);

    public Boolean deserialize(T data);
}
