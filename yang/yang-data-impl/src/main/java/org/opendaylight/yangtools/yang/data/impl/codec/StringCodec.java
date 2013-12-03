package org.opendaylight.yangtools.yang.data.impl.codec;

public interface StringCodec<T> {

    Class<T> getInputClass();
    
    String serialize(T data);
    T deserialize (String stringRepresentation);
}
