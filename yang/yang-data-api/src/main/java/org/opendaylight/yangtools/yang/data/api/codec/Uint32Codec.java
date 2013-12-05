package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;

import com.google.common.primitives.UnsignedLong;

/**
 * 
 * FIXME: Should be changed to {@link UnsignedLong}
 * 
 * @author ttkacik
 *
 * @param <T>
 */
public interface Uint32Codec<T>  extends Codec<T,Long> {

    public T serialize(Long data);

    public Long deserialize(T data);
}
