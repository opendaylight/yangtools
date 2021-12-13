/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import java.util.Collection;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;
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
        extends AbstractOriginalStmtCtx<A, D, E> implements ResumedStatement {
    private final String rawArgument;

    private StatementMap substatements = StatementMap.empty();
    private @Nullable D declaredInstance;

    // Copy constructor
    AbstractResumedStatement(final AbstractResumedStatement<A, D, E> original) {
        super(original);
        this.rawArgument = original.rawArgument;
        this.substatements = original.substatements;
        this.declaredInstance = original.declaredInstance;
    }

    AbstractResumedStatement(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference ref,
            final String rawArgument) {
        super(def, ref);
        this.rawArgument = def.support().internArgument(rawArgument);
    }

    @Override
    public final String rawArgument() {
        return rawArgument;
    }

    @Override
    public Collection<? extends StatementContextBase<?, ?, ?>> mutableDeclaredSubstatements() {
        return verifyNotNull(substatements);
    }

    @Override
    public final D declared() {
        final D existing;
        return (existing = declaredInstance) != null ? existing : loadDeclared();
    }

    private @NonNull D loadDeclared() {
        final ModelProcessingPhase phase = getCompletedPhase();
        checkState(phase == ModelProcessingPhase.FULL_DECLARATION || phase == ModelProcessingPhase.EFFECTIVE_MODEL,
                "Cannot build declared instance after phase %s", phase);
        return declaredInstance = definition().getFactory().createDeclared(this, substatementsAsDeclared());
    }

    private @NonNull Stream<DeclaredStatement<?>> substatementsAsDeclared() {
        var stream = substatements.stream();
        if (getImplicitDeclaredFlag()) {
            stream = stream.map(stmt -> {
                var ret = stmt;
                while (ret.origin() == StatementOrigin.CONTEXT) {
                    final var stmts = ret.substatements;
                    verify(stmts.size() == 1, "Unexpected substatements %s", stmts);
                    ret = verifyNotNull(stmts.get(0));
                }
                return ret;
            });
        }

        return stream.map(AbstractResumedStatement::declared);
    }

    @Override
    public final StatementDefinition getDefinition() {
        return publicDefinition();
    }

    @Override
    public final StatementSourceReference getSourceReference() {
        return sourceReference();
    }

    @Override
    public final boolean isFullyDefined() {
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
                "Declared statement cannot be added in effective phase at: %s", sourceReference());

        final var implicitParent = definition().getImplicitParentFor(this, def.getPublicView());
        if (implicitParent.isPresent()) {
            return createImplicitParent(offset, implicitParent.orElseThrow(), ref, argument)
                .createSubstatement(0, def, ref, argument);
        }

        final AbstractResumedStatement<X, Y, Z> ret = new SubstatementContext<>(this, def, ref, argument);
        substatements = substatements.put(offset, ret);
        def.onStatementAdded(ret);
        return ret;
    }

    @Override
    final Stream<? extends @NonNull StmtContext<?, ?, ?>> streamDeclared() {
        return substatements.stream().filter(StmtContext::isSupportedToBuildEffective);
    }

    @Override
    final void dropDeclaredSubstatements() {
        substatements = null;
    }

    /**
     * Attempt to lookup a declared substatement by its offset in this statement, passing through any implicit
     * statements which have been created to encapsulate it.
     *
     * @param offset Substatement offset
     * @return Substatement, or null if substatement does not exist.
     */
    final @Nullable AbstractResumedStatement<?, ?, ?> enterSubstatement(final int offset) {
        var ret = substatements.get(offset);
        if (ret != null) {
            while (ret.origin() == StatementOrigin.CONTEXT) {
                ret = verifyNotNull(ret.substatements.get(0));
            }
        }
        return ret;
    }

    /**
     * End the specified phase for this statement and return this statement's declared parent statement.
     *
     * @param phase processing phase that ended
     * @return Declared parent statement
     */
    final @Nullable AbstractResumedStatement<?, ?, ?> exitStatement(final ModelProcessingPhase phase) {
        finishDeclaration(phase);
        final var parent = getParentContext();
        if (parent == null) {
            return null;
        }

        var ret = verifyParent(parent);
        // Unwind all undeclared statements
        while (ret.origin() == StatementOrigin.CONTEXT) {
            ret.finishDeclaration(phase);
            ret = verifyParent(ret.getParentContext());
        }
        return ret;
    }

    // FIXME: AbstractResumedStatement should only ever have AbstractResumedStatement parents, which would remove the
    //        need for this method. In ordered to do that we need to untangle SubstatementContext's users and do not
    //        allow it being reparent()ed.
    private static AbstractResumedStatement<?, ?, ?> verifyParent(final StatementContextBase<?, ?, ?> parent) {
        verify(parent instanceof AbstractResumedStatement, "Unexpected parent context %s", parent);
        return (AbstractResumedStatement<?, ?, ?>) parent;
    }

    final void resizeSubstatements(final int expectedSize) {
        substatements = substatements.ensureCapacity(expectedSize);
    }

    final void declarationFinished(final ModelProcessingPhase phase) {
        finishChildrenDeclaration(phase);
        finishDeclaration(phase);
    }

    private void finishChildrenDeclaration(final ModelProcessingPhase phase) {
        checkState(isFullyDefined());
        substatements.forEach(stmt -> stmt.declarationFinished(phase));
    }

    /**
     * Ends declared section of current node for the specified phase.
     *
     * @param phase processing phase that ended
     */
    private void finishDeclaration(final ModelProcessingPhase phase) {
        definition().onDeclarationFinished(this, phase);
    }

    private AbstractResumedStatement<?, ?, ?> createImplicitParent(final int offset,
            final StatementSupport<?, ?, ?> implicitParent, final StatementSourceReference ref, final String argument) {
        setImplicitDeclaredFlag();
        return createSubstatement(offset, new StatementDefinitionContext<>(implicitParent),
            ImplicitSubstatement.of(ref), argument);
    }
}
