package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

public interface SubmoduleNamespace extends StatementNamespace<ModuleIdentifier, SubmoduleStatement, EffectiveStatement<String, SubmoduleStatement>> {

}
