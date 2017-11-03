package org.opendaylight.yangtools.rfc6536.model.api;

import java.net.URI;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;

/**
 * Constants associated with RFC6536.
 *
 * @author Robert Varga
 */
public final class NACMConstants {
    private static final String MODULE_NAME = "ietf-netconf-acm";
    private static final URI MODULE_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:ietf-netconf-acm");
    private static final Revision RFC6536_REVISION = Revision.of("2012-02-22");

    /**
     * Runtime RFC6536 identity.
     */
    public static final QNameModule RFC6536_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC6536_REVISION).intern();

    /**
     * RFC6536 model source name.
     */
    public static final RevisionSourceIdentifier RFC6536_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        RFC6536_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC6536_SOURCE}.
     */
    public static final String MODULE_PREFIX = "nacm";

    private NACMConstants() {
        throw new UnsupportedOperationException();
    }
}
