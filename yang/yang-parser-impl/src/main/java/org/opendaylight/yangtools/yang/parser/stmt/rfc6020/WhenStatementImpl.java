package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorAppTagStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ErrorMessageStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WhenStatementImpl extends AbstractDeclaredStatement<RevisionAwareXPath> implements
        WhenStatement {

    protected WhenStatementImpl(
            StmtContext<RevisionAwareXPath, WhenStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<RevisionAwareXPath,WhenStatement,EffectiveStatement<RevisionAwareXPath,WhenStatement>> {

        public Definition() {
            super(Rfc6020Mapping.When);
        }

        @Override public RevisionAwareXPath parseArgumentValue(
                StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return new RevisionAwareXPathImpl(value, false);
        }

        @Override public WhenStatement createDeclared(
                StmtContext<RevisionAwareXPath, WhenStatement, ?> ctx) {
            return new WhenStatementImpl(ctx);
        }

        @Override public EffectiveStatement<RevisionAwareXPath, WhenStatement> createEffective(
                StmtContext<RevisionAwareXPath, WhenStatement, EffectiveStatement<RevisionAwareXPath, WhenStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull @Override
    public RevisionAwareXPath getCondition() {
        return argument();
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
