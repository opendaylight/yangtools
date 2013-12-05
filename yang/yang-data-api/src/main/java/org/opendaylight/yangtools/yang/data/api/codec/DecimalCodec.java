package org.opendaylight.yangtools.yang.data.api.codec;

import java.math.BigDecimal;


public interface DecimalCodec<T> {

    public T serialize(BigDecimal data);

    public BigDecimal deserialize(T data);
}
