package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;


public interface EmptyCodec<T>  extends Codec<T,Void> {

    public T serialize(Void data);

    public Void deserialize(T data);
}
