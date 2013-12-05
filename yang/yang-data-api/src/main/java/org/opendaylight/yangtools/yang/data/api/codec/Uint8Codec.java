package org.opendaylight.yangtools.yang.data.api.codec;

/**
 * 
 * FIXME: Should be changed to UnsignedByte
 * 
 * @author ttkacik
 *
 * @param <T>
 */
public interface Uint8Codec<T> {

    public T serialize(Short data);

    public Short deserialize(T data);
}
