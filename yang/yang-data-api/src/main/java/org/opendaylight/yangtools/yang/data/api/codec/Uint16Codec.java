package org.opendaylight.yangtools.yang.data.api.codec;


public interface Uint16Codec<T> {

    public T serialize(Integer data);

    public Integer deserialize(T data);
}
