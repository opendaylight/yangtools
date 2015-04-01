package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YangVersionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;

public class YangVersionStatementImpl extends AbstractDeclaredStatement<String> implements YangVersionStatement {

    protected YangVersionStatementImpl(StmtContext<String, YangVersionStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,YangVersionStatement,EffectiveStatement<String,YangVersionStatement>> {

        public Definition() {
            super(Rfc6020Mapping.YangVersion);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return value;
        }

        @Override
        public YangVersionStatement createDeclared(StmtContext<String, YangVersionStatement, ?> ctx) {
            return new YangVersionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, YangVersionStatement> createEffective(StmtContext<String, YangVersionStatement, EffectiveStatement<String, YangVersionStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull @Override
    public String getValue() {
        return rawArgument();
    }
}
