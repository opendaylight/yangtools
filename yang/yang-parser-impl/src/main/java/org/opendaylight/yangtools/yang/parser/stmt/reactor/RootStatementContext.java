/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * root statement class for a Yang source
 */
public class RootStatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> extends
        StatementContextBase<A, D, E> {

    private final SourceSpecificContext sourceContext;
    private final A argument;

    RootStatementContext(ContextBuilder<A, D, E> builder, SourceSpecificContext sourceContext) throws SourceException {
        super(builder);
        this.sourceContext = sourceContext;
        this.argument = builder.getDefinition().parseArgumentValue(this, builder.getRawArgument());
    }

    RootStatementContext(RootStatementContext<A, D, E> original, QNameModule newQNameModule, TypeOfCopy typeOfCopy)
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
    private void copyDeclaredStmts(RootStatementContext<A, D, E> original, QNameModule newQNameModule,
            TypeOfCopy typeOfCopy) throws SourceException {
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
    private void copyEffectiveStmts(RootStatementContext<A, D, E> original, QNameModule newQNameModule,
            TypeOfCopy typeOfCopy) throws SourceException {
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
     * @throws SourceException
     */
    @Override
    public StatementContextBase<?, ?, ?> createCopy(StatementContextBase<?, ?, ?> newParent, TypeOfCopy typeOfCopy)
            throws SourceException {
        return createCopy(null, newParent, typeOfCopy);
    }

    /**
     * @return copy of this considering {@link TypeOfCopy} (augment, uses)
     *
     * @throws SourceException
     */
    @Override
    public StatementContextBase<A, D, E> createCopy(QNameModule newQNameModule,
            StatementContextBase<?, ?, ?> newParent, TypeOfCopy typeOfCopy) throws SourceException {
        RootStatementContext<A, D, E> copy = new RootStatementContext<>(this, newQNameModule, typeOfCopy);

        copy.addAllToCopyHistory(this.getCopyHistory());
        copy.addToCopyHistory(typeOfCopy);

        if(this.getOriginalCtx() != null) {
            copy.setOriginalCtx(this.getOriginalCtx());
        } else {
            copy.setOriginalCtx(this);
        }

        return copy;
    }

    /**
     * @return this' argument as it is the only from root (this)
     */
    @Override
    public List<Object> getArgumentsFromRoot() {
        List<Object> argumentList = new LinkedList<>();
        argumentList.add(argument);
        return argumentList;
    }

    /**
     * @return this as it is the only\context from root (this)
     */
    @Override
    public List<StmtContext<?, ?, ?>> getStmtContextsFromRoot() {
        List<StmtContext<?, ?, ?>> stmtContextsList = new LinkedList<>();
        stmtContextsList.add(this);
        return stmtContextsList;
    }

    /**
     * @return true
     */
    @Override
    public boolean isRootContext() {
        return true;
    }

}
