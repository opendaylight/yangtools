/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementFactory;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
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
        extends OriginalStmtCtx<A, D, E> implements ResumedStatement {
    private final String rawArgument;

    private StatementMap substatements = StatementMap.empty();
    private @Nullable D declaredInstance;
    private boolean implicitDeclared;
    private boolean fullyDefined;

    // Copy constructor
    AbstractResumedStatement(final AbstractResumedStatement<A, D, E> original) {
        super(original);
        rawArgument = original.rawArgument;
        substatements = original.substatements;
        declaredInstance = original.declaredInstance;
        fullyDefined = original.fullyDefined;
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
        return verifyNotNull(substatements, "Substatements no longer available in %s", this);
    }

    @Override
    public final D declared() {
        final D existing;
        return (existing = declaredInstance) != null ? existing : loadDeclared();
    }

    private @NonNull D loadDeclared() {
        final var phase = getCompletedPhase();
        return switch (phase) {
            case FULL_DECLARATION, EFFECTIVE_MODEL ->
                declaredInstance = definition().getFactory().createDeclared(this, substatementsAsDeclared());
            default -> throw new IllegalStateException("Cannot build declared instance after phase " + phase);
        };
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private @NonNull Stream<DeclaredStatement<?>> substatementsAsDeclared() {
        final Stream<AbstractResumedStatement<?, ?, ?>> stream;
        if (implicitDeclared) {
            stream = substatements.stream().map(AbstractResumedStatement::unmaskUndeclared);
        } else {
            stream = (Stream) substatements.stream();
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
        return fullyDefined;
    }

    final void setFullyDefined() {
        fullyDefined = true;
    }

    @Override
    final E createEffective(final StatementFactory<A, D, E> factory) {
        return createEffective(factory, this, streamDeclared(), streamEffective());
    }

    // Creates EffectiveStatement through full materialization and assumes declared statement presence
    private @NonNull E createEffective(final StatementFactory<A, D, E> factory,
            final StatementContextBase<A, D, E> ctx, final Stream<? extends StmtContext<?, ?, ?>> declared,
            final Stream<? extends ReactorStmtCtx<?, ?, ?>> effective) {
        // Statement reference count infrastructure makes an assumption that effective statement is only built after
        // the declared statement is already done. Statements tracked by this class always have a declared view, and
        // we need to ensure that is built before we touch effective substatements.
        //
        // Once the effective substatement stream has been exhausted, reference counting will triggers a sweep, hence
        // the substatements may be gone by the time the factory attempts to acquire the declared statement.
        ctx.declared();

        return factory.createEffective(ctx, orderSubstatements(declared, effective));
    }

    private static @NonNull Stream<StmtContext<?, ?, ?>> orderSubstatements(
            final Stream<? extends StmtContext<?, ?, ?>> declaredSubstatements,
            final Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements) {
        /*
         * This dance is required to ensure that effects of 'uses' nodes are applied in the same order as
         * the statements were defined -- i.e. if we have something like this:
         *
         * container foo {
         *   uses bar;
         *   uses baz;
         * }
         *
         * grouping baz {
         *   leaf baz {
         *     type string;
         *   }
         * }
         *
         * grouping bar {
         *   leaf bar {
         *     type string;
         *   }
         * }
         *
         * The reactor would first inline 'uses baz' as that definition is the first one completely resolved and then
         * inline 'uses bar'. Here we are iterating in declaration order re-inline the statements.
         *
         * FIXME: 7.0.0: this really should be handled by UsesStatementSupport such that 'uses baz' would have a
         *               prerequisite of a resolved 'uses bar'.
         */
        final var declaredInit = declaredSubstatements
            .filter(StmtContext::isSupportedByFeatures)
            .collect(Collectors.toList());

        final var substatementsInit = new ArrayList<StmtContext<?, ?, ?>>();
        Set<StmtContext<?, ?, ?>> filteredStatements = null;
        for (var declaredSubstatement : declaredInit) {
            substatementsInit.add(declaredSubstatement);

            // FIXME: YANGTOOLS-1161: we need to integrate this functionality into the reactor, so that this
            //                        transformation is something reactor's declared statements already take into
            //                        account.
            final Collection<? extends StmtContext<?, ?, ?>> effect = declaredSubstatement.getEffectOfStatement();
            if (!effect.isEmpty()) {
                if (filteredStatements == null) {
                    filteredStatements = new HashSet<>();
                }
                filteredStatements.addAll(effect);
                // Note: we need to filter here to exclude unsupported statements
                effect.stream().filter(StmtContext::isSupportedToBuildEffective).forEach(substatementsInit::add);
            }
        }

        final Stream<? extends StmtContext<?, ?, ?>> effective;
        if (filteredStatements != null) {
            final var filtered = filteredStatements;
            effective = effectiveSubstatements.filter(stmt -> !filtered.contains(stmt));
        } else {
            effective = effectiveSubstatements;
        }

        substatementsInit.addAll(effective.collect(Collectors.toList()));
        return substatementsInit.stream();
    }

    @Override
    final E createInferredEffective(final StatementFactory<A, D, E> factory,
            final InferredStatementContext<A, D, E> ctx, final Stream<? extends ReactorStmtCtx<?, ?, ?>> declared,
            final Stream<? extends ReactorStmtCtx<?, ?, ?>> effective) {
        return createEffective(factory, ctx, declared, effective);
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
        final var inProgressPhase = getRoot().getSourceContext().getInProgressPhase();
        checkState(inProgressPhase != ModelProcessingPhase.EFFECTIVE_MODEL,
                "Declared statement cannot be added in effective phase at: %s", sourceReference());

        final SubstatementContext<X, Y, Z> ret;
        final var implicitParent = definition().getImplicitParentFor(this, def.getPublicView());
        if (implicitParent.isPresent()) {
            implicitDeclared = true;
            final var parent = createUndeclared(offset, implicitParent.orElseThrow(), ref, argument);
            ret = new SubstatementContext<>(parent, def, ref, argument);
            parent.addEffectiveSubstatement(ret);
        } else {
            ret = new SubstatementContext<>(this, def, ref, argument);
            substatements = substatements.put(offset, ret);
        }

        def.onStatementAdded(ret);
        return ret;
    }

    private <X, Y extends DeclaredStatement<X>, Z extends EffectiveStatement<X, Y>>
            UndeclaredStmtCtx<X, Y, Z> createUndeclared(final int offset, final StatementSupport<X, Y, Z> support,
                final StatementSourceReference ref, final String argument) {
        final UndeclaredStmtCtx<X, Y, Z> ret;
        final var implicitParent = definition().getImplicitParentFor(this, support.getPublicView());
        if (implicitParent.isPresent()) {
            final var parent = createUndeclared(offset, implicitParent.orElseThrow(), ref, argument);
            ret = new ImplicitStmtCtx<>(parent, support, argument);
            parent.addEffectiveSubstatement(ret);
        } else {
            ret = new ImplicitStmtCtx<>(this, support, argument);
            substatements = substatements.put(offset, ret);
        }
        support.onStatementAdded(ret);
        return ret;
    }

    @Override
    final Stream<? extends @NonNull ReactorStmtCtx<?, ?, ?>> streamDeclared() {
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
        final var stmt = substatements.get(offset);
        return stmt == null ? null : unmaskUndeclared(stmt);
    }

    private static @NonNull AbstractResumedStatement<?, ?, ?> unmaskUndeclared(final ReactorStmtCtx<?, ?, ?> stmt) {
        var ret = stmt;
        while (!(ret instanceof AbstractResumedStatement<?, ?, ?> resumed)) {
            if (ret instanceof UndeclaredStmtCtx<?, ?, ?> undeclared) {
                ret = undeclared.getResumedSubstatement();
            } else {
                throw new VerifyException("Unexpected statement " + ret);
            }
        }
        return resumed;
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
        while (!(ret instanceof AbstractResumedStatement<?, ?, ?> resumed)) {
            ret.finishDeclaration(phase);
            ret = verifyParent(ret.getParentContext());
        }
        return resumed;
    }

    // FIXME: AbstractResumedStatement should only ever have OriginalStmtCtx parents, which would remove the need for
    //        this method. In ordered to do that we need to untangle SubstatementContext's users and do not allow it
    //        being reparent()ed.
    private static OriginalStmtCtx<?, ?, ?> verifyParent(final StatementContextBase<?, ?, ?> parent) {
        if (parent instanceof OriginalStmtCtx<?, ?, ?> original) {
            return original;
        }
        throw new VerifyException("Unexpected parent context " + parent);
    }

    final void resizeSubstatements(final int expectedSize) {
        substatements = substatements.ensureCapacity(expectedSize);
    }

    @Override
    final void declarationFinished(final ModelProcessingPhase phase) {
        finishChildrenDeclaration(phase);
        finishDeclaration(phase);
    }

    private void finishChildrenDeclaration(final ModelProcessingPhase phase) {
        checkState(isFullyDefined());
        substatements.forEach(stmt -> stmt.declarationFinished(phase));
    }
}
