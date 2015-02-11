package org.opendaylight.yangtools.yang.parser.base;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class ModuleStatementImpl extends AbstractRootStatement<ModuleStatement> implements ModuleStatement {

    private NamespaceStatement namespace;

    protected ModuleStatementImpl(StmtContext<String, ModuleStatement,?> context) {
        super(context);
    }

    public static class Definition extends StatementSupport<String, ModuleStatement, EffectiveStatement<String,ModuleStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Module);
        }

        @Override
        public String parseArgumentValue(StmtContext<?,?,?> ctx, String value) {
            return value;
        }

        @Override
        public ModuleStatement createDeclared(StmtContext<String, ModuleStatement,?> ctx) {
            return new ModuleStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String,ModuleStatement> createEffective(StmtContext<String, ModuleStatement,EffectiveStatement<String,ModuleStatement>> ctx) {
            throw new UnsupportedOperationException();
        }

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
