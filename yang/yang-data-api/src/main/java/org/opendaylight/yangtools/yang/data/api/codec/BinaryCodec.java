package org.opendaylight.yangtools.yang.data.api.codec;


public interface BinaryCodec<T> {

    public T serialize(byte[] data);

    public byte[] deserialize(T data);
}
