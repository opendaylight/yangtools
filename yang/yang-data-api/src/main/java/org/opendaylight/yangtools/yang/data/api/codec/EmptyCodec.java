package org.opendaylight.yangtools.yang.data.api.codec;


public interface EmptyCodec<T> {

    public T serialize(Void data);

    public Void deserialize(T data);
}
