package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public interface StatementFactory<A,D extends DeclaredStatement<A>,E extends EffectiveStatement<A, D>> {

    D createDeclared(StmtContext<A,D,?> ctx);

    E createEffective(StmtContext<A,D,E> ctx);

}
