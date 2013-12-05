package org.opendaylight.yangtools.yang.data.api.codec;

import java.math.BigDecimal;

import org.opendaylight.yangtools.concepts.Codec;


public interface DecimalCodec<T>  extends Codec<T,BigDecimal> {

    public T serialize(BigDecimal data);

    public BigDecimal deserialize(T data);
}
