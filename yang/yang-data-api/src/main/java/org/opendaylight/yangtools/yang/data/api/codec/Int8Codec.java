package org.opendaylight.yangtools.yang.data.api.codec;


public interface Int8Codec<T> {

    public T serialize(Byte data);

    public Byte deserialize(T data);
}
