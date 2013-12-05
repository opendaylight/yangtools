package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;


public interface Int64Codec<T> extends Codec<T,Long> {

    public T serialize(Long data);

    public Long deserialize(T data);
}
