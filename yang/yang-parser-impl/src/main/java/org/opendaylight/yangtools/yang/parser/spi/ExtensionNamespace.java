package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

public interface ExtensionNamespace extends StatementNamespace<QName, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> {

}
