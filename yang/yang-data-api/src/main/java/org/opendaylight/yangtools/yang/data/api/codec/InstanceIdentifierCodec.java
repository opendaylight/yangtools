package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.yang.common.QName;

public interface InstanceIdentifierCodec<T> {

    public T serialize(QName data);

    public QName deserialize(T data);
}
