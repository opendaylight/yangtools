package org.opendaylight.yangtools.yang.data.api.codec;


public interface Int16Codec<T> {

    public T serialize(Short data);

    public Short deserialize(T data);
}
