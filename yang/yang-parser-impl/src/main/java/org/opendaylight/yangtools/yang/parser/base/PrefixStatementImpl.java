package org.opendaylight.yangtools.yang.parser.base;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class PrefixStatementImpl extends AbstractDeclaredStatement<String> implements PrefixStatement {

    public static class Definition extends StatementSupport<String,PrefixStatement,EffectiveStatement<String,PrefixStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Namespace);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?,?> ctx, String value) {
            return (value);
        }

        @Override
        public PrefixStatement createDeclared(StmtContext<String, PrefixStatement,?> ctx) {
            return new PrefixStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String,PrefixStatement> createEffective(StmtContext<String, PrefixStatement,EffectiveStatement<String,PrefixStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    PrefixStatementImpl(StmtContext<String, PrefixStatement,?> context) {
        super(context);
    }

   @Override
public java.lang.String getValue() {
    return rawArgument();
}
}
