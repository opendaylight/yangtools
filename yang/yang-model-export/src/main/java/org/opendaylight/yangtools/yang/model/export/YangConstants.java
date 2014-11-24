package org.opendaylight.yangtools.yang.model.export;

import java.net.URI;
import org.opendaylight.yangtools.yang.common.QNameModule;

// FIXME: Should be probably moved to yang-common
class YangConstants {
    public final static URI YANG_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:1");
    public final static URI YIN_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:yin:1");
    public final static QNameModule YANG_MODULE = QNameModule.create(YANG_NAMESPACE, null);
    public final static QNameModule YIN_MODULE = QNameModule.create(YIN_NAMESPACE, null);



}
