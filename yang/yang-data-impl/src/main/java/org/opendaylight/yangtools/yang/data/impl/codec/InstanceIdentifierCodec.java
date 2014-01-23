package org.opendaylight.yangtools.yang.data.impl.codec;

import org.opendaylight.yangtools.yang.binding.BindingCodec;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface InstanceIdentifierCodec extends BindingCodec<org.opendaylight.yangtools.yang.data.api.InstanceIdentifier,InstanceIdentifier<?>> {

    @Override
    public org.opendaylight.yangtools.yang.data.api.InstanceIdentifier serialize(InstanceIdentifier<?> input);

    @Override
    public InstanceIdentifier<?> deserialize(org.opendaylight.yangtools.yang.data.api.InstanceIdentifier input);
}
