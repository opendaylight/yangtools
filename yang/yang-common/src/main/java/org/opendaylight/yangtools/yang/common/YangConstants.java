package org.opendaylight.yangtools.yang.common;

import java.net.URI;


/**
 *
 * Constant definitions present in RFC6020 for YANG 1.0
 *
 *
 */
public class YangConstants {
    public final static URI YANG_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:1");
    public final static URI YIN_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:yin:1");
    public final static QNameModule YANG_MODULE = QNameModule.create(YANG_NAMESPACE, null);
    public final static QNameModule YIN_MODULE = QNameModule.create(YIN_NAMESPACE, null);



}