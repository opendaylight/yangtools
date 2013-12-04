package org.opendaylight.yangtools.yang.data.api.codec;

import java.util.Set;


public interface BitsCodec<T> {

    public T serialize(Set<String> data);

    public Set<String> deserialize(T data);
}
