package org.opendaylight.yangtools.yang.data.impl.schema.builder.api;

import org.opendaylight.yangtools.yang.common.QName;

import java.util.Map;

public interface AttributesBuilder<B> {
    public B withAttributes(Map<QName, String> attributes);
}
