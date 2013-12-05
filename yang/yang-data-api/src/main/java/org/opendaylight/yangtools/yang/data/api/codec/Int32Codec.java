package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;


public interface Int32Codec<T> extends Codec<T,Integer> {

    public T serialize(Integer data);

    public Integer deserialize(T data);
}
