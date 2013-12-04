package org.opendaylight.yangtools.yang.data.api.codec;

import java.math.BigInteger;


public interface Uint64Codec<T> {

    public T serialize(BigInteger data);

    public BigInteger deserialize(T data);
}
