package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.common.QName;


public interface IdentityrefCodec<T>  extends Codec<T,QName> {

    public T serialize(QName data);

    public QName deserialize(T data);
}
