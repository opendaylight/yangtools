package org.opendaylight.yangtools.yang.data.api.codec;


public interface Int64Codec<T> {

    public T serialize(Long data);

    public Long deserialize(T data);
}
