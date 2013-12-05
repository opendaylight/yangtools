package org.opendaylight.yangtools.yang.data.api.codec;


public interface BooleanCodec<T> {

    public T serialize(Boolean data);

    public Boolean deserialize(T data);
}
