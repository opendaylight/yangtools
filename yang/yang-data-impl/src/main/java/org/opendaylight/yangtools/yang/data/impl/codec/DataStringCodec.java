package org.opendaylight.yangtools.yang.data.impl.codec;

import org.opendaylight.yangtools.concepts.Codec;

public interface DataStringCodec<T> extends Codec<String, T> {
    
    Class<T> getInputClass();

    String serialize(T data);

    T deserialize(String stringRepresentation);
}
