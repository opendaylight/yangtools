package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class DefinedTypeEffectiveStatement<T extends TypeDefinition<T>> implements TypeEffectiveStatement<TypeStatement> {
    DefinedTypeEffectiveStatement(final StmtContext<QName, TypedefStatement, ?> ctx) {

    }

    @Override
    public TypeEffectiveStatement<TypeStatement> derive(final EffectiveStatement<?, TypeStatement> stmt, final SchemaPath path) {
        throw new UnsupportedOperationException("Should never be derived?");
    }


    @Override
    public TypeStatement getDeclared() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public StatementDefinition statementDefinition() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String argument() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public StatementSource getStatementSource() {
        // TODO Auto-generated method stub
        return null;
    }
}
