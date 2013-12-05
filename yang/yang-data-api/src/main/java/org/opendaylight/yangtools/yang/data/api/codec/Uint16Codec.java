package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;

import com.google.common.primitives.UnsignedInteger;

/**
 * 
 * 
 * FIXME: In Helium release this codec should be changed to
 * {@link UnsignedShort}
 * 
 * @author ttkacik
 *
 * @param <T>
 */
public interface Uint16Codec<T> extends Codec<T,Integer> {

    public T serialize(Integer data);

    public Integer deserialize(T data);
}
