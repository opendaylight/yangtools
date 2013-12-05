package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;


public interface BinaryCodec<T> extends Codec<T, byte[]>{

    public T serialize(byte[] data);

    public byte[] deserialize(T data);
}
