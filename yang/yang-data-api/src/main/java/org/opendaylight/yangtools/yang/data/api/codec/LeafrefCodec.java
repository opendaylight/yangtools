package org.opendaylight.yangtools.yang.data.api.codec;



public interface LeafrefCodec<T> {

    public T serialize(Object data);

    public Object deserialize(T data);
}
