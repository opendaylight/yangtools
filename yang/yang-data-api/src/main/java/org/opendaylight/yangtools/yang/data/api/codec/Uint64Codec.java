package org.opendaylight.yangtools.yang.data.api.codec;

import java.math.BigInteger;

import org.opendaylight.yangtools.concepts.Codec;

import com.google.common.primitives.UnsignedLong;

/**
 * FIXME: Should be changed to {@link UnsignedLong}
 * 
 * @author ttkacik
 *
 * @param <T>
 */
public interface Uint64Codec<T> extends Codec<T,BigInteger> {

    public T serialize(BigInteger data);

    public BigInteger deserialize(T data);
}
