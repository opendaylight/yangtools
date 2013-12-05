package org.opendaylight.yangtools.yang.data.api.codec;


public interface Uint32Codec<T> {

    public T serialize(Long data);

    public Long deserialize(T data);
}
