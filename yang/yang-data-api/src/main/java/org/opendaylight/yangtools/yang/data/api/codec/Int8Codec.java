package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;


public interface Int8Codec<T> extends Codec<T,Byte> {

    public T serialize(Byte data);

    public Byte deserialize(T data);
}
