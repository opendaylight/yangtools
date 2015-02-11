package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;


class RootStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
    extends StatementContextBase<A, D,E> {


    private final SourceSpecificContext sourceContext;

    RootStatementContext(ContextBuilder<A, D,E> builder, SourceSpecificContext sourceContext) throws SourceException {
        super(builder);
        this.sourceContext = sourceContext;
    }

    @Override
    public StatementContextBase<?,?, ?> getParentContext() {
        return null;
    }

    @Override
    public NamespaceStorageNode getParentNamespaceStorage() {
        return sourceContext;
    }

    @Override
    public Registry getBehaviourRegistry() {
        return sourceContext;
    }

    @Override
    public RootStatementContext<?,?,?> getRoot() {
        return this;
    }

    SourceSpecificContext getSourceContext() {
        return sourceContext;
    }



}
