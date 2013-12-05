package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;


public interface Int16Codec<T>  extends Codec<T,Short>{

    public T serialize(Short data);

    public Short deserialize(T data);
}
