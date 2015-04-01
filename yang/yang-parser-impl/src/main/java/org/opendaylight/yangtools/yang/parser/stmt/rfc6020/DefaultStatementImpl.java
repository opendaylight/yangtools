package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;

public class DefaultStatementImpl extends AbstractDeclaredStatement<String> implements
        DefaultStatement {

    protected DefaultStatementImpl(
            StmtContext<String, DefaultStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,DefaultStatement,EffectiveStatement<String,DefaultStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Default);
        }

        @Override public String parseArgumentValue(
                StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return value;
        }

        @Override public DefaultStatement createDeclared(
                StmtContext<String, DefaultStatement, ?> ctx) {
            return new DefaultStatementImpl(ctx);
        }

        @Override public EffectiveStatement<String, DefaultStatement> createEffective(
                StmtContext<String, DefaultStatement, EffectiveStatement<String, DefaultStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull @Override
    public String getValue() {
        return rawArgument();
    }
}
