module org.opendaylight.yangtools.yang.data.codec.xml {
    exports org.opendaylight.yangtools.yang.data.codec.xml;

    requires transitive org.opendaylight.yangtools.yang.data.api;
    requires transitive org.opendaylight.yangtools.yang.model.api;
    requires transitive org.opendaylight.yangtools.rfc8528.data.api;

    requires org.codehaus.stax2;
    requires org.opendaylight.yangtools.util;
    requires org.opendaylight.yangtools.yang.model.util;
    requires org.opendaylight.yangtools.yang.data.util;
    requires org.opendaylight.yangtools.yang.data.impl;
    requires org.opendaylight.yangtools.rfc7952.model.api;
    requires org.opendaylight.yangtools.rfc8528.model.api;
    requires org.opendaylight.yangtools.rfc8528.data.util;
    requires org.slf4j;
}