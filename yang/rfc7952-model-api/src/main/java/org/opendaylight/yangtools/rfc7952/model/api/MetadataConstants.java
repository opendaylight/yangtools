package org.opendaylight.yangtools.rfc7952.model.api;

import java.net.URI;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;

/**
 * Constants associated with RFC7952.
 *
 * @author Robert Varga
 */
public final class MetadataConstants {
    private static final String MODULE_NAME = "ietf-yang-metadata";
    private static final URI MODULE_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:ietf-yang-metadata");
    private static final Revision RFC7952_REVISION = Revision.of("2016-08-05");

    /**
     * Runtime RFC6536 identity.
     */
    public static final QNameModule RFC7952_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC7952_REVISION).intern();

    /**
     * RFC6536 model source name.
     */
    public static final RevisionSourceIdentifier RFC7952_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        RFC7952_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC7952_SOURCE}.
     */
    public static final String MODULE_PREFIX = "md";

    private MetadataConstants() {
        throw new UnsupportedOperationException();
    }
}
