package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RevisionStatementImpl extends AbstractDeclaredStatement<String> implements RevisionStatement {

    protected RevisionStatementImpl(
            StmtContext<String, RevisionStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<String,RevisionStatement,EffectiveStatement<String,RevisionStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Revision);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return value;
        }

        @Override
        public RevisionStatement createDeclared(StmtContext<String, RevisionStatement, ?> ctx) {
            return new RevisionStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, RevisionStatement> createEffective(StmtContext<String, RevisionStatement, EffectiveStatement<String, RevisionStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String getDate() {
        return rawArgument();
    }

    @Nullable @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Nullable @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }
}
