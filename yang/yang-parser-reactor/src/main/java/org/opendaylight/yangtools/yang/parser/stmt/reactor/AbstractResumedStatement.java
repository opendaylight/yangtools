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

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
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
 * <p>
 * NOTE: this class is visible only for migration purposes until we get rid of
 *       {@link #appendImplicitStatement(StatementSupport)}, at which point this class will be hidden become hidden.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class AbstractResumedStatement<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> implements ResumedStatement {
    private final @NonNull StatementDefinitionContext<A, D, E> definition;
    private final @NonNull StatementSourceReference statementDeclSource;
    private final StmtContext<?, ?, ?> originalCtx;
    private final StmtContext<?, ?, ?> prevCopyCtx;
    private final String rawArgument;

    private StatementMap substatements = StatementMap.empty();

    AbstractResumedStatement(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final String rawArgument) {
        this.definition = requireNonNull(def);
        this.statementDeclSource = requireNonNull(ref);
        this.rawArgument = def.internArgument(rawArgument);
        this.originalCtx = null;
        this.prevCopyCtx = null;
    }

    AbstractResumedStatement(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final String rawArgument, final CopyType copyType) {
        super(CopyHistory.of(copyType, CopyHistory.original()));
        this.definition = requireNonNull(def);
        this.statementDeclSource = requireNonNull(ref);
        this.rawArgument = rawArgument;
        this.originalCtx = null;
        this.prevCopyCtx = null;
    }

    AbstractResumedStatement(final AbstractResumedStatement<A, D, E> original) {
        super(original);
        this.definition = original.definition;
        this.statementDeclSource = original.statementDeclSource;
        this.rawArgument = original.rawArgument;
        this.originalCtx = original.getOriginalCtx().orElse(original);
        this.prevCopyCtx = original;
        this.substatements = original.substatements;
    }

    @Override
    public final Optional<StmtContext<?, ?, ?>> getOriginalCtx() {
        return Optional.ofNullable(originalCtx);
    }

    @Override
    public final Optional<? extends StmtContext<?, ?, ?>> getPreviousCopyCtx() {
        return Optional.ofNullable(prevCopyCtx);
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
    public Collection<? extends StmtContext<?, ?, ?>> declaredSubstatements() {
        return substatements.values();
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return substatements.values();
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

    // YANG example: RPC/action statements always have 'input' and 'output' defined
    // FIXME: consider the design here, as they seem to have an effective input/output, determined just before they
    //        complete 'fully declared' phase, i.e. if we are finishing full definition, we should be able to seed
    //        effective statements with those implicits.
    @Deprecated
    public void appendImplicitStatement(final StatementSupport<?, ?, ?> statementToAdd) {
        createSubstatement(substatements.capacity(), new StatementDefinitionContext<>(statementToAdd),
                ImplicitSubstatement.of(getStatementSourceReference()), null);
    }

    @Override
    protected final StatementDefinitionContext<A, D, E> definition() {
        return definition;
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
        substatements.values().forEach(stmt -> {
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
