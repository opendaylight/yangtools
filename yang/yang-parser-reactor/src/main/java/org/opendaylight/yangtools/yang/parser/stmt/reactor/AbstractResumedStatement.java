/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ImplicitSubstatement;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter.ResumedStatement;

/**
 * Intermediate subclass of StatementContextBase facing the parser stream via implementation of ResumedStatement. This
 * shields inference-type substatements from these details.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
abstract class AbstractResumedStatement<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> implements ResumedStatement {
    private final @NonNull StatementSourceReference statementDeclSource;
    private final String rawArgument;

    private StatementMap substatements = StatementMap.empty();
    private @Nullable D declaredInstance;

    // Copy constructor
    AbstractResumedStatement(final AbstractResumedStatement<A, D, E> original) {
        super(original);
        this.statementDeclSource = original.statementDeclSource;
        this.rawArgument = original.rawArgument;
        this.substatements = original.substatements;
        this.declaredInstance = original.declaredInstance;
    }

    AbstractResumedStatement(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final String rawArgument) {
        super(def);
        this.statementDeclSource = requireNonNull(ref);
        this.rawArgument = def.support().internArgument(rawArgument);
    }

    AbstractResumedStatement(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final String rawArgument, final CopyType copyType) {
        super(def, CopyHistory.of(copyType, CopyHistory.original()));
        this.statementDeclSource = requireNonNull(ref);
        this.rawArgument = rawArgument;
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
    public final StatementSourceReference getStatementSourceReference() {
        return statementDeclSource;
    }

    @Override
    public final String rawStatementArgument() {
        return rawArgument;
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return substatements;
    }

    @Override
    public final D buildDeclared() {
        final D existing = declaredInstance;
        if (existing != null) {
            return existing;
        }
        final ModelProcessingPhase phase = getCompletedPhase();
        checkState(phase == ModelProcessingPhase.FULL_DECLARATION || phase == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Cannot build declared instance after phase %s", phase);
        return declaredInstance = definition().getFactory().createDeclared(this);
    }

    @Override
    public @NonNull StatementDefinition getDefinition() {
        return getPublicDefinition();
    }

    @Override
    public @NonNull StatementSourceReference getSourceReference() {
        return getStatementSourceReference();
    }

    @Override
    public boolean isFullyDefined() {
        return fullyDefined();
    }

    /**
     * Create a new substatement at the specified offset.
     *
     * @param offset Substatement offset
     * @param def definition context
     * @param ref source reference
     * @param argument statement argument
     * @param <X> new substatement argument type
     * @param <Y> new substatement declared type
     * @param <Z> new substatement effective type
     * @return A new substatement
     */
    @SuppressWarnings("checkstyle:methodTypeParameterName")
    final <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            AbstractResumedStatement<X, Y, Z> createSubstatement(final int offset,
                    final StatementDefinitionContext<X, Y, Z> def, final StatementSourceReference ref,
                    final String argument) {
        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        checkState(inProgressPhase != ModelProcessingPhase.EFFECTIVE_MODEL,
                "Declared statement cannot be added in effective phase at: %s", getStatementSourceReference());

        final Optional<StatementSupport<?, ?, ?>> implicitParent =
                definition().getImplicitParentFor(def.getPublicView());
        if (implicitParent.isPresent()) {
            return createImplicitParent(offset, implicitParent.get(), ref, argument).createSubstatement(offset, def,
                    ref, argument);
        }

        final AbstractResumedStatement<X, Y, Z> ret = new SubstatementContext<>(this, def, ref, argument);
        substatements = substatements.put(offset, ret);
        def.onStatementAdded(ret);
        return ret;
    }

    @Override
    final boolean hasEmptySubstatements() {
        return substatements.size() == 0 && hasEmptyEffectiveSubstatements();
    }

    /**
     * Lookup substatement by its offset in this statement.
     *
     * @param offset Substatement offset
     * @return Substatement, or null if substatement does not exist.
     */
    final AbstractResumedStatement<?, ?, ?> lookupSubstatement(final int offset) {
        return substatements.get(offset);
    }

    final void resizeSubstatements(final int expectedSize) {
        substatements = substatements.ensureCapacity(expectedSize);
    }

    final void walkChildren(final ModelProcessingPhase phase) {
        checkState(isFullyDefined());
        substatements.forEach(stmt -> {
            stmt.walkChildren(phase);
            stmt.endDeclared(phase);
        });
    }

    private AbstractResumedStatement<?, ?, ?> createImplicitParent(final int offset,
            final StatementSupport<?, ?, ?> implicitParent, final StatementSourceReference ref, final String argument) {
        final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(implicitParent);
        return createSubstatement(offset, def, ImplicitSubstatement.of(ref), argument);
    }
}
