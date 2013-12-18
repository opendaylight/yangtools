package org.opendaylight.yangtools.yang.data.impl.util;

import java.net.URI;
import java.util.Date;

import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;

public interface NodeBuilder<P extends Node<?>, T extends NodeBuilder<P, T>> extends Builder<P> {

    T setQName(QName name);
    
    QName getQName();

    T setAttribute(QName attrName, String attrValue);

    T setAttribute(String attrName, String attrValue);

}
