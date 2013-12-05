package org.opendaylight.yangtools.yang.data.api.codec;


public interface Int32Codec<T> {

    public T serialize(Integer data);

    public Integer deserialize(T data);
}
