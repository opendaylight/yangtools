package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public interface QNameToStatementDefinition extends IdentifierNamespace<QName, StatementDefinition> {

}
