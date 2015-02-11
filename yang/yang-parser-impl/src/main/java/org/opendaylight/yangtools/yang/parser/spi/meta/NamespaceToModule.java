package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;

public interface NamespaceToModule extends StatementNamespace<QNameModule, ModuleStatement, EffectiveStatement<String, ModuleStatement>> {

}
