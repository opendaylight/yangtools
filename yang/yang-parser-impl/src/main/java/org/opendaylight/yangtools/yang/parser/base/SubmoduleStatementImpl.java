package org.opendaylight.yangtools.yang.parser.base;

import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class SubmoduleStatementImpl extends AbstractRootStatement<SubmoduleStatement> implements SubmoduleStatement {

    protected SubmoduleStatementImpl(StmtContext<String, SubmoduleStatement> context) {
        super(context);
    }

    public static class Definition extends StatementSupport<String,SubmoduleStatement> {

        public Definition() {
            super(Rfc6020Mapping.Submodule);
        }

        @Override
        public String parseArgumentValue(StmtContext<String, SubmoduleStatement> ctx, String value) {
            return value;
        }

        @Override
        public SubmoduleStatement createDeclared(StmtContext<String, SubmoduleStatement> ctx) {
            return new SubmoduleStatementImpl(ctx);
        }

        @Override
        public SubmoduleStatement createEffective(StmtContext<String, SubmoduleStatement> ctx) {
            return new SubmoduleStatementImpl(ctx);
        }

    }

    @Override
    public SubmoduleStatement get() {
        return this;
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
    public BelongsToStatement getBelongsTo() {
        return firstDeclared(BelongsToStatement.class);
    }

}
