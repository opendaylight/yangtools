package org.opendaylight.yangtools.yang.common;

import java.net.URI;


/**
 *
 * Constant definitions present in YANG langaue related RFCs.
 *
 *
 */
public final class YangConstants {

    public final static URI RFC6020_YANG_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:1");
    public final static URI RFC6020_YIN_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:yin:1");
    public final static QNameModule RFC6020_YANG_MODULE = QNameModule.cachedReference(QNameModule.create(RFC6020_YANG_NAMESPACE, null));
    public final static QNameModule RFC6020_YIN_MODULE = QNameModule.cachedReference(QNameModule.create(RFC6020_YIN_NAMESPACE, null));

    private YangConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

}