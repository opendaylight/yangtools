package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.net.URI;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NamespaceStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class NamespaceStatementImpl extends AbstractDeclaredStatement<URI> implements NamespaceStatement {

    public static class Definition extends AbstractStatementSupport<URI,NamespaceStatement,EffectiveStatement<URI,NamespaceStatement>> {

        public Definition() {
            super(org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping.Namespace);
        }

        @Override
        public URI parseArgumentValue(StmtContext<?, ?,?> ctx, String value) {
            return URI.create(value);
        }

        @Override
        public NamespaceStatement createDeclared(StmtContext<URI, NamespaceStatement,?> ctx) {
            return new NamespaceStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<URI,NamespaceStatement> createEffective(StmtContext<URI, NamespaceStatement,EffectiveStatement<URI,NamespaceStatement>> ctx) {
            throw new UnsupportedOperationException();
        }

    }

    NamespaceStatementImpl(StmtContext<URI, NamespaceStatement,?> context) {
        super(context);
    }

    @Override
    public URI getUri() {
        return argument();
    }
}
