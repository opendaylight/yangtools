package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;

public class MandatoryStatementImpl extends AbstractDeclaredStatement<String> implements
        MandatoryStatement {

    protected MandatoryStatementImpl(
            StmtContext<String, MandatoryStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,MandatoryStatement,EffectiveStatement<String,MandatoryStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Mandatory);
        }

        @Override public String parseArgumentValue(
                StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return value;
        }

        @Override public MandatoryStatement createDeclared(
                StmtContext<String, MandatoryStatement, ?> ctx) {
            return new MandatoryStatementImpl(ctx);
        }

        @Override public EffectiveStatement<String, MandatoryStatement> createEffective(
                StmtContext<String, MandatoryStatement, EffectiveStatement<String, MandatoryStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull @Override
    public String getValue() {
        return rawArgument();
    }
}
