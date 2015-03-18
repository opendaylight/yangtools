package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;

public class TypeStatementImpl extends AbstractDeclaredStatement<String> implements TypeStatement {

    protected TypeStatementImpl(StmtContext<String, TypeStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,TypeStatement,EffectiveStatement<String,TypeStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Type);
        }

        @Override public String parseArgumentValue(StmtContext<?, ?, ?> ctx,
                String value) throws SourceException {
            return value;
        }

        @Override public TypeStatement createDeclared(
                StmtContext<String, TypeStatement, ?> ctx) {
            return new TypeStatementImpl(ctx);
        }

        @Override public EffectiveStatement<String, TypeStatement> createEffective(
                StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull @Override
    public String getName() {
        return argument();
    }
}
