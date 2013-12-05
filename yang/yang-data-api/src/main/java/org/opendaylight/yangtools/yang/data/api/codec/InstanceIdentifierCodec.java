package org.opendaylight.yangtools.yang.data.api.codec;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;

public interface InstanceIdentifierCodec<T>  extends Codec<T,InstanceIdentifier> {

    public T serialize(InstanceIdentifier data);

    public InstanceIdentifier deserialize(T data);
}
