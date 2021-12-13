/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractEagerStmtCtx<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> {
    private List<ReactorStmtCtx<?, ?, ?>> effective = ImmutableList.of();

    AbstractEagerStmtCtx(final AbstractEagerStmtCtx<A, D, E> original) {
        super(original);
    }

    AbstractEagerStmtCtx(final StatementDefinitionContext<A, D, E> def) {
        super(def);
    }

    AbstractEagerStmtCtx(final StatementDefinitionContext<A, D, E> def, final CopyType copyType) {
        super(def, copyType);
    }

    @Override
    public final Optional<StmtContext<A, D, E>> getOriginalCtx() {
        return Optional.empty();
    }

    @Override
    public final Optional<StmtContext<A, D, E>> getPreviousCopyCtx() {
        return Optional.empty();
    }

    @Override
    public final Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        return mutableEffectiveSubstatements(effective);
    }

    @Override
    public final void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef) {
        effective = removeStatementFromEffectiveSubstatements(effective, statementDef);
    }

    @Override
    public final void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef,
            final String statementArg) {
        effective = removeStatementFromEffectiveSubstatements(effective, statementDef, statementArg);
    }

    @Override
    public final void addEffectiveSubstatement(final Mutable<?, ?, ?> substatement) {
        effective = addEffectiveSubstatement(effective, substatement);
    }

    @Override
    final void addEffectiveSubstatementsImpl(final Collection<? extends Mutable<?, ?, ?>> statements) {
        effective = addEffectiveSubstatementsImpl(effective, statements);
    }

    @Override
    final Iterable<ReactorStmtCtx<?, ?, ?>> effectiveChildrenToComplete() {
        return effective;
    }

    @Override
    final Stream<? extends @NonNull StmtContext<?, ?, ?>> streamEffective() {
        return effective.stream().filter(StmtContext::isSupportedToBuildEffective);
    }
}
