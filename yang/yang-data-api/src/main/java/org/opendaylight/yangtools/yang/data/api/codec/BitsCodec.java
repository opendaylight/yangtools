package org.opendaylight.yangtools.yang.data.api.codec;

import java.util.Set;

import org.opendaylight.yangtools.concepts.Codec;


public interface BitsCodec<T> extends Codec<T, Set<String>>{

    public T serialize(Set<String> data);

    public Set<String> deserialize(T data);
}
