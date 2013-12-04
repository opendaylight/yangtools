package org.opendaylight.yangtools.yang.data.api.codec;


public interface StringCodec<T> {

    public T serialize(String data);

    public String deserialize(T data);
}
