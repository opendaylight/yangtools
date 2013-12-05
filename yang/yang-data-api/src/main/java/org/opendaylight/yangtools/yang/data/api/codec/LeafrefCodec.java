package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;



public interface LeafrefCodec<T> extends Codec<T,Object> {

    public T serialize(Object data);

    public Object deserialize(T data);
}
