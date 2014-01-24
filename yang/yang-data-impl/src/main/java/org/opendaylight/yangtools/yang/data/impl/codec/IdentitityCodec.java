package org.opendaylight.yangtools.yang.data.impl.codec;

import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BindingCodec;
import org.opendaylight.yangtools.yang.common.QName;

public interface IdentitityCodec<T extends BaseIdentity> extends BindingCodec<QName, Class<T>>{

    @Override
    public QName serialize(Class<T> input);

    @Override
    public Class<T> deserialize(QName input);
}
