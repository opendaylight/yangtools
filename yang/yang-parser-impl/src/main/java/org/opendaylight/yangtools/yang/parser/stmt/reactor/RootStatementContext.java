/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Optional;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * root statement class for a Yang source
 */
public class RootStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends
        StatementContextBase<A, D, E> {

    private final SourceSpecificContext sourceContext;
    private final A argument;

    RootStatementContext(final ContextBuilder<A, D, E> builder, final SourceSpecificContext sourceContext) throws SourceException {
        super(builder);
        this.sourceContext = sourceContext;
        this.argument = builder.getDefinition().parseArgumentValue(this, builder.getRawArgument());
    }

    RootStatementContext(final RootStatementContext<A, D, E> original, final QNameModule newQNameModule, final TypeOfCopy typeOfCopy)
            throws SourceException {
        super(original);

        sourceContext = original.sourceContext;
        this.argument = original.argument;

        copyDeclaredStmts(original, newQNameModule, typeOfCopy);

        copyEffectiveStmts(original, newQNameModule, typeOfCopy);

    }

    /**
     * copies declared statements from original to this' substatements
     *
     * @param typeOfCopy
     *            determines whether copy is used by augmentation or uses
     * @throws SourceException
     */
    private void copyDeclaredStmts(final RootStatementContext<A, D, E> original, final QNameModule newQNameModule,
            final TypeOfCopy typeOfCopy) throws SourceException {
        Collection<? extends StmtContext<?, ?, ?>> originalDeclaredSubstatements = original.declaredSubstatements();
        for (StmtContext<?, ?, ?> stmtContext : originalDeclaredSubstatements) {
            this.addEffectiveSubstatement(stmtContext.createCopy(newQNameModule, this, typeOfCopy));
        }
    }

    /**
     * copies effective statements from original to this' substatements
     *
     * @param typeOfCopy
     *            determines whether copy is used by augmentation or uses
     * @throws SourceException
     */
    private void copyEffectiveStmts(final RootStatementContext<A, D, E> original, final QNameModule newQNameModule,
            final TypeOfCopy typeOfCopy) throws SourceException {
        Collection<? extends StmtContext<?, ?, ?>> originalEffectiveSubstatements = original.effectiveSubstatements();
        for (StmtContext<?, ?, ?> stmtContext : originalEffectiveSubstatements) {
            this.addEffectiveSubstatement(stmtContext.createCopy(newQNameModule, this, typeOfCopy));
        }
    }

    /**
     * @return null as root cannot have parent
     */
    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return null;
    }

    /**
     * @return namespace storage of source context
     */
    @Override
    public NamespaceStorageNode getParentNamespaceStorage() {
        return sourceContext;
    }

    /**
     * @return registry of source context
     */
    @Override
    public Registry getBehaviourRegistry() {
        return sourceContext;
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.ROOT_STATEMENT_LOCAL;
    }
    /**
     * @return this as its own root
     */
    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return this;
    }

    SourceSpecificContext getSourceContext() {
        return sourceContext;
    }

    @Override
    public A getStatementArgument() {
        return argument;
    }

    /**
     * @return copy of this considering {@link TypeOfCopy} (augment, uses)
     *
     * @throws SourceException instance of SourceException
     */
    @Override
    public StatementContextBase<?, ?, ?> createCopy(final StatementContextBase<?, ?, ?> newParent, final TypeOfCopy typeOfCopy)
            throws SourceException {
        return createCopy(null, newParent, typeOfCopy);
    }

    /**
     * @return copy of this considering {@link TypeOfCopy} (augment, uses)
     *
     * @throws SourceException instance of SourceException
     */
    @Override
    public StatementContextBase<A, D, E> createCopy(final QNameModule newQNameModule,
            final StatementContextBase<?, ?, ?> newParent, final TypeOfCopy typeOfCopy) throws SourceException {
        RootStatementContext<A, D, E> copy = new RootStatementContext<>(this, newQNameModule, typeOfCopy);

        copy.addAllToCopyHistory(this.getCopyHistory());
        copy.addToCopyHistory(typeOfCopy);

        if(this.getOriginalCtx() != null) {
            copy.setOriginalCtx(this.getOriginalCtx());
        } else {
            copy.setOriginalCtx(this);
        }
        definition().onStatementAdded(copy);
        return copy;
    }

    @Override
    public Optional<SchemaPath> getSchemaPath() {
        return Optional.of(SchemaPath.ROOT);
    }

    /**
     * @return true
     */
    @Override
    public boolean isRootContext() {
        return true;
    }
}
