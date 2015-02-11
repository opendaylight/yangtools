package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

public interface PrefixToModule extends IdentifierNamespace<String, QNameModule> {

    public static final String DEFAULT_NAMESPACE = "";

}
