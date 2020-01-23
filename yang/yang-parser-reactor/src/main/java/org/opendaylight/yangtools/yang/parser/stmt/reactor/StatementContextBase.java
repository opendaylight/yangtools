/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelActionBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.ImplicitSubstatement;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter.ResumedStatement;

public abstract class StatementContextBase<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends AbstractStmtContext<A, D, E> implements ResumedStatement {
    /**
     * Event listener when an item is added to model namespace.
     */
    interface OnNamespaceItemAdded extends EventListener {
        /**
         * Invoked whenever a new item is added to a namespace.
         */
        void namespaceItemAdded(AbstractStmtContext<?, ?, ?> context, Class<?> namespace, Object key, Object value);
    }

    /**
     * Event listener when a parsing {@link ModelProcessingPhase} is completed.
     */
    interface OnPhaseFinished extends EventListener {
        /**
         * Invoked whenever a processing phase has finished.
         */
        boolean phaseFinished(AbstractStmtContext<?, ?, ?> context, ModelProcessingPhase finishedPhase);
    }

    /**
     * Interface for all mutations within an {@link ModelActionBuilder.InferenceAction}.
     */
    interface ContextMutation {

        boolean isFinished();
    }

    private final @NonNull StatementDefinitionContext<A, D, E> definition;
    private final @NonNull StatementSourceReference statementDeclSource;
    private final StmtContext<?, ?, ?> originalCtx;
    private final StmtContext<?, ?, ?> prevCopyCtx;
    private final String rawArgument;

    private List<Mutable<?, ?, ?>> effective = ImmutableList.of();
    private StatementMap substatements = StatementMap.empty();

    StatementContextBase(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final String rawArgument) {
        super(CopyHistory.original());
        this.definition = requireNonNull(def);
        this.statementDeclSource = requireNonNull(ref);
        this.rawArgument = def.internArgument(rawArgument);
        this.originalCtx = null;
        this.prevCopyCtx = null;
    }

    StatementContextBase(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final String rawArgument, final CopyType copyType) {
        super(CopyHistory.of(copyType, CopyHistory.original()));
        this.definition = requireNonNull(def);
        this.statementDeclSource = requireNonNull(ref);
        this.rawArgument = rawArgument;
        this.originalCtx = null;
        this.prevCopyCtx = null;
    }

    StatementContextBase(final StatementContextBase<A, D, E> original) {
        super(original.getCopyHistory());
        this.definition = original.definition;
        this.statementDeclSource = original.statementDeclSource;
        this.rawArgument = original.rawArgument;
        this.originalCtx = original.getOriginalCtx().orElse(original);
        this.prevCopyCtx = original;
        this.substatements = original.substatements;
        this.effective = original.effective;
    }

    @Override
    public Optional<StmtContext<?, ?, ?>> getOriginalCtx() {
        return Optional.ofNullable(originalCtx);
    }

    @Override
    public Optional<? extends StmtContext<?, ?, ?>> getPreviousCopyCtx() {
        return Optional.ofNullable(prevCopyCtx);
    }

    @Override
    public StatementSource getStatementSource() {
        return statementDeclSource.getStatementSource();
    }

    @Override
    public StatementSourceReference getStatementSourceReference() {
        return statementDeclSource;
    }

    @Override
    public final String rawStatementArgument() {
        return rawArgument;
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableDeclaredSubstatements() {
        return substatements.values();
    }

    @Override
    public Collection<? extends Mutable<?, ?, ?>> mutableEffectiveSubstatements() {
        if (effective instanceof ImmutableCollection) {
            return effective;
        }

        return Collections.unmodifiableCollection(effective);
    }

    private void shrinkEffective() {
        if (effective.isEmpty()) {
            effective = ImmutableList.of();
        }
    }

    @Override
    public void removeStatementFromEffectiveSubstatements(final StatementDefinition statementDef) {
        if (!effective.isEmpty()) {
            removeStatement(effective.iterator(), statementDef);
            shrinkEffective();
        }
    }

    @Override
    final void doRemoveStatement(final StatementDefinition statementDef, final String statementArg) {
        if (!effective.isEmpty()) {
            removeStatement(effective.iterator(), statementDef, statementArg);
            shrinkEffective();
        }
    }

    @Override
    protected void appendEffectiveStatement(final Mutable<?, ?, ?> substatement) {
        beforeAddEffectiveStatement(1);
        effective.add(substatement);
    }

    @Override
    protected void appendEffectiveStatements(final Collection<? extends Mutable<?, ?, ?>> statements) {
        beforeAddEffectiveStatement(statements.size());
        effective.addAll(statements);
    }

    private void beforeAddEffectiveStatement(final int toAdd) {
        if (effective.isEmpty()) {
            effective = new ArrayList<>(toAdd);
        }
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
    public final <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            StatementContextBase<X, Y, Z> createSubstatement(final int offset,
                    final StatementDefinitionContext<X, Y, Z> def, final StatementSourceReference ref,
                    final String argument) {
        final ModelProcessingPhase inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        checkState(inProgressPhase != ModelProcessingPhase.EFFECTIVE_MODEL,
                "Declared statement cannot be added in effective phase at: %s", getStatementSourceReference());

        final Optional<StatementSupport<?, ?, ?>> implicitParent = definition.getImplicitParentFor(def.getPublicView());
        if (implicitParent.isPresent()) {
            return createImplicitParent(offset, implicitParent.get(), ref, argument).createSubstatement(offset, def,
                    ref, argument);
        }

        final StatementContextBase<X, Y, Z> ret = new SubstatementContext<>(this, def, ref, argument);
        substatements = substatements.put(offset, ret);
        def.onStatementAdded(ret);
        return ret;
    }

    private StatementContextBase<?, ?, ?> createImplicitParent(final int offset,
            final StatementSupport<?, ?, ?> implicitParent, final StatementSourceReference ref, final String argument) {
        final StatementDefinitionContext<?, ?, ?> def = new StatementDefinitionContext<>(implicitParent);
        return createSubstatement(offset, def, ImplicitSubstatement.of(ref), argument);
    }

    public void appendImplicitStatement(final StatementSupport<?, ?, ?> statementToAdd) {
        createSubstatement(substatements.capacity(), new StatementDefinitionContext<>(statementToAdd),
                ImplicitSubstatement.of(getStatementSourceReference()), null);
    }

    /**
     * Lookup substatement by its offset in this statement.
     *
     * @param offset Substatement offset
     * @return Substatement, or null if substatement does not exist.
     */
    final StatementContextBase<?, ?, ?> lookupSubstatement(final int offset) {
        return substatements.get(offset);
    }

    final void resizeSubstatements(final int expectedSize) {
        substatements = substatements.ensureCapacity(expectedSize);
    }

    final void walkChildren(final ModelProcessingPhase phase) {
        checkState(fullyDefined());
        substatements.values().forEach(stmt -> {
            stmt.walkChildren(phase);
            stmt.endDeclared(phase);
        });
    }

    /**
     * Return the context in which this statement was defined.
     *
     * @return statement definition
     */
    @Override
    protected final @NonNull StatementDefinitionContext<A, D, E> definition() {
        return definition;
    }

    @Override
    final Collection<? extends AbstractStmtContext<?, ?, ?>> declared() {
        return substatements.values();
    }

    @Override
    final Collection<? extends Mutable<?, ?, ?>> effective() {
        return effective;
    }

    @Override
    public StatementDefinition getDefinition() {
        return getPublicDefinition();
    }

    @Override
    public StatementSourceReference getSourceReference() {
        return getStatementSourceReference();
    }

    @Override
    public boolean isFullyDefined() {
        return fullyDefined();
    }
}
