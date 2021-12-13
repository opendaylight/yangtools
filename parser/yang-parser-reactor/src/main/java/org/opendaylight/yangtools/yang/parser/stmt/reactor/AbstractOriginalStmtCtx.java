/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractOriginalStmtCtx<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractOriginalStmtCtx.class);

    private final @NonNull StatementSourceReference ref;

    private List<ReactorStmtCtx<?, ?, ?>> effective = ImmutableList.of();

    AbstractOriginalStmtCtx(final AbstractOriginalStmtCtx<A, D, E> original) {
        super(original);
        this.ref = original.ref;
    }

    AbstractOriginalStmtCtx(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref) {
        super(def);
        this.ref = requireNonNull(ref);
    }

    AbstractOriginalStmtCtx(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final CopyType copyType) {
        super(def, copyType);
        this.ref = requireNonNull(ref);
    }

    @Override
    public final StatementSourceReference sourceReference() {
        return ref;
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
    final Iterator<ReactorStmtCtx<?, ?, ?>> effectiveChildrenToComplete() {
        return effective.iterator();
    }

    @Override
    final Stream<? extends @NonNull StmtContext<?, ?, ?>> streamEffective() {
        return effective.stream().filter(StmtContext::isSupportedToBuildEffective);
    }

    @Override
    final AbstractOriginalStmtCtx<A, D, E> unmodifiedEffectiveSource() {
        // This statement is comes from the source
        return this;
    }

    @Override
    final boolean hasEmptySubstatements() {
        return effective.isEmpty() && mutableDeclaredSubstatements().isEmpty();
    }

    @Override
    final boolean noSensitiveSubstatements() {
        return hasEmptySubstatements()
            || noSensitiveSubstatements(effective) && noSensitiveSubstatements(mutableDeclaredSubstatements());
    }

    @Override
    final void markNoParentRef() {
        markNoParentRef(mutableDeclaredSubstatements());
        markNoParentRef(effective);
    }

    @Override
    final int sweepSubstatements() {
        // First we need to sweep all statements, which may trigger sweeps all across the place, for example:
        // - 'effective' member sweeping a 'substatements' member
        // - 'substatements' member sweeping a 'substatements' member which came before it during iteration
        // We then iterate once again, counting what remains unswept
        final var declared = mutableDeclaredSubstatements();

        sweep(declared);
        sweep(effective);
        final int count = countUnswept(declared) + countUnswept(effective);
        if (count != 0) {
            LOG.debug("{} children left to sweep from {}", count, this);
        }
        effective = null;
        dropDeclaredSubstatements();
        return count;
    }

    abstract void dropDeclaredSubstatements();
}
