package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;

/**
 * 
 * FIXME: Should be changed to UnsignedByte
 * 
 * @author ttkacik
 *
 * @param <T>
 */
public interface Uint8Codec<T> extends Codec<T,Short>{

    public T serialize(Short data);

    public Short deserialize(T data);
}
