package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

public interface ModuleNamespace extends StatementNamespace<ModuleIdentifier, ModuleStatement, EffectiveStatement<String, ModuleStatement>> {

}
