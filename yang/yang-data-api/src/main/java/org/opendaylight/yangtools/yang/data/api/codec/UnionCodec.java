package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;

public interface UnionCodec<T> extends Codec<T,String> {

    public T serialize(String data);

    public String deserialize(T data);
}
