package org.opendaylight.yangtools.yang.data.api.codec;

public interface UnionCodec<T> {

    public T serialize(String data);

    public String deserialize(T data);
}
