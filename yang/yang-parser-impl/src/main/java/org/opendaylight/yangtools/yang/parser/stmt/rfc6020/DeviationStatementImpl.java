package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DeviationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;

public class DeviationStatementImpl extends AbstractDeclaredStatement<SchemaNodeIdentifier> implements DeviationStatement {

    protected DeviationStatementImpl(
            StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<SchemaNodeIdentifier,DeviationStatement,EffectiveStatement<SchemaNodeIdentifier,DeviationStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Deviation);
        }

        @Override public SchemaNodeIdentifier parseArgumentValue(
                StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            return SchemaNodeIdentifier.create(true, Utils.qNameFromArgument(ctx, value));
        }

        @Override public DeviationStatement createDeclared(
                StmtContext<SchemaNodeIdentifier, DeviationStatement, ?> ctx) {
            return new DeviationStatementImpl(ctx);
        }

        @Override public EffectiveStatement<SchemaNodeIdentifier, DeviationStatement> createEffective(
                StmtContext<SchemaNodeIdentifier, DeviationStatement, EffectiveStatement<SchemaNodeIdentifier, DeviationStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull @Override
    public SchemaNodeIdentifier getTargetNode() {
        return argument();
    }
}
