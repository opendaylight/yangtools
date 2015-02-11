package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class ModuleStatementImpl extends AbstractRootStatement<ModuleStatement> implements ModuleStatement {

    private NamespaceStatement namespace;

    protected ModuleStatementImpl(StmtContext<String, ModuleStatement,?> context) {
        super(context);
    }

    @Override
    public String getName() {
        return rawArgument();
    }

    @Override
    public YangVersionStatement getYangVersion() {
        return firstDeclared(YangVersionStatement.class);
    }

    @Override
    public NamespaceStatement getNamespace() {
        return namespace;
    }

    @Override
    public PrefixStatement getPrefix() {
        return firstDeclared(PrefixStatement.class);
    }

}
