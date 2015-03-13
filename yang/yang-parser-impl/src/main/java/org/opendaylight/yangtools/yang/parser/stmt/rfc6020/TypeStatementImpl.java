package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class TypeStatementImpl extends AbstractDeclaredStatement<QName> implements TypeStatement {

    protected TypeStatementImpl(StmtContext<QName, TypeStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<QName,TypeStatement,EffectiveStatement<QName,TypeStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Type);
        }

        @Override public QName parseArgumentValue(StmtContext<?, ?, ?> ctx,
                String value) throws SourceException {
            return Utils.qNameFromArgument(ctx,value);
        }

        @Override public TypeStatement createDeclared(
                StmtContext<QName, TypeStatement, ?> ctx) {
            return new TypeStatementImpl(ctx);
        }

        @Override public EffectiveStatement<QName, TypeStatement> createEffective(
                StmtContext<QName, TypeStatement, EffectiveStatement<QName, TypeStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

    @Nonnull @Override public String getName() {
        return rawArgument();
    }
}
