package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviateStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;

public class DeviateStatementImpl extends AbstractDeclaredStatement<String> implements DeviateStatement {

    protected DeviateStatementImpl(
            StmtContext<String, DeviateStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,DeviateStatement,EffectiveStatement<String,DeviateStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Deviate);
        }

        @Override public String parseArgumentValue(
                StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return value;
        }

        @Override public DeviateStatement createDeclared(
                StmtContext<String, DeviateStatement, ?> ctx) {
            return new DeviateStatementImpl(ctx);
        }

        @Override public EffectiveStatement<String, DeviateStatement> createEffective(
                StmtContext<String, DeviateStatement, EffectiveStatement<String, DeviateStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull @Override
    public String getValue() {
        return rawArgument();
    }
}
