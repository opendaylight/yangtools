package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public interface NodeIdentifierWithPredicatesCodec {
    YangInstanceIdentifier.NodeIdentifierWithPredicates serialize(QName parentQName,Identifier<?> key);
}
