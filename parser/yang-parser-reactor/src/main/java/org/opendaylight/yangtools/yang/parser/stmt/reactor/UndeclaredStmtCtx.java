/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

/**
/**
 * Core reactor statement implementation of {@link Mutable}.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
final class UndeclaredStmtCtx<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends AbstractEagerStmtCtx<A, D, E> {
    private final StatementContextBase<?, ?, ?> parent;
    private final A argument;

    UndeclaredStmtCtx(final StatementContextBase<?, ?, ?> parent, final StatementDefinitionContext<A, D, E> def,
            final A argument) {
        super(def);
        this.parent = requireNonNull(parent, "Parent must not be null");
        this.argument = argument != null ? argument : def.parseArgumentValue(this, null);
    }

    @Override
    public @NonNull StatementSourceReference sourceReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String rawArgument() {
        throw new UnsupportedOperationException();
    }

    @Override
    public D declared() {
        throw new UnsupportedOperationException();
    }

    @Override
    void addEffectiveSubstatementsImpl(final Collection<? extends Mutable<?, ?, ?>> statements) {
        // TODO Auto-generated method stub

    }

    @Override
    Iterable<ReactorStmtCtx<?, ?, ?>> effectiveChildrenToComplete() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    Stream<? extends @NonNull StmtContext<?, ?, ?>> streamDeclared() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    Stream<? extends @NonNull StmtContext<?, ?, ?>> streamEffective() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    boolean noSensitiveSubstatements() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    StatementContextBase<A, D, E> reparent(final StatementContextBase<?, ?, ?> newParent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    boolean hasEmptySubstatements() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    ReactorStmtCtx<A, D, E> unmodifiedEffectiveSource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    void markNoParentRef() {
        // TODO Auto-generated method stub

    }

    @Override
    int sweepSubstatements() {
        // TODO Auto-generated method stub
        return 0;
    }



    /*
     * KEEP THINGS ORGANIZED!
     *
     * below methods exist in the same form in InferredStatementContext. If any adjustment is made here, make sure it is
     * properly updated there.
     */
    @Override
    public A argument() {
        return argument;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return parent;
    }

    @Override
    public StorageNodeType getStorageNodeType() {
        return StorageNodeType.STATEMENT_LOCAL;
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentNamespaceStorage() {
        return parent;
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

    @Override
    public EffectiveConfig effectiveConfig() {
        return effectiveConfig(parent);
    }

    @Override
    protected boolean isIgnoringIfFeatures() {
        return isIgnoringIfFeatures(parent);
    }

    @Override
    protected boolean isIgnoringConfig() {
        return isIgnoringConfig(parent);
    }

    @Override
    protected boolean isParentSupportedByFeatures() {
        return parent.isSupportedByFeatures();
    }
}
