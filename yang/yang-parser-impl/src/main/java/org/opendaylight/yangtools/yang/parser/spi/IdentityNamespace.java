package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;

public interface IdentityNamespace extends IdentifierNamespace<QName, IdentityStatement> {


}
