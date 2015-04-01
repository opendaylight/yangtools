package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContactStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

public class KeyStatementImpl extends AbstractDeclaredStatement<Collection<SchemaNodeIdentifier>> implements KeyStatement{

    protected KeyStatementImpl(StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<Collection<SchemaNodeIdentifier>,KeyStatement,EffectiveStatement<Collection<SchemaNodeIdentifier>,KeyStatement>> {

        public Definition() {
            super(Rfc6020Mapping.Key);
        }

        @Override
        public Collection<SchemaNodeIdentifier> parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException {
            //FIXME: return proper value
            return null;
        }

        @Override
        public KeyStatement createDeclared(StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?> ctx) {
            return new KeyStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement> createEffective(StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, EffectiveStatement<Collection<SchemaNodeIdentifier>, KeyStatement>> ctx) {
            throw new UnsupportedOperationException();
        }
    }

}
