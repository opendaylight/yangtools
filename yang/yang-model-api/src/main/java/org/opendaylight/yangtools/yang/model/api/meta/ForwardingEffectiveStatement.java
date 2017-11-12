package org.opendaylight.yangtools.yang.model.api.meta;

import com.google.common.collect.ForwardingObject;
import java.util.Collection;
import java.util.Map;

public abstract class ForwardingEffectiveStatement<A, D extends DeclaredStatement<A>,
        E extends EffectiveStatement<A, D>> extends ForwardingObject implements EffectiveStatement<A, D> {

    @Override
    protected abstract E delegate();


    @Override
    public D getDeclared() {
        return delegate().getDeclared();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        return delegate().get(namespace, identifier);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        return delegate().getAll(namespace);
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return delegate().effectiveSubstatements();
    }

    @Override
    public StatementDefinition statementDefinition() {
        return delegate().statementDefinition();
    }

    @Override
    public A argument() {
        return delegate().argument();
    }

    @Override
    public StatementSource getStatementSource() {
        return delegate().getStatementSource();
    }
}
