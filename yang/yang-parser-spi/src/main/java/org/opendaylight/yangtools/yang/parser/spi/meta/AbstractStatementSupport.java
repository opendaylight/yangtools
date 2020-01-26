/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Class providing necessary support for processing a YANG statement. This class is intended to be subclassed
 * by developers who want to add semantic support for a statement to a parser reactor.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
public abstract class AbstractStatementSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        implements StatementDefinition, StatementFactory<A, D, E>, StatementSupport<A, D, E> {

    private final @NonNull StatementDefinition type;

    protected AbstractStatementSupport(final StatementDefinition publicDefinition) {
        this.type = requireNonNull(publicDefinition);
        checkArgument(publicDefinition != this);
    }

    @Override
    public final StatementDefinition getPublicView() {
        return type;
    }

    @Override
    public void onStatementAdded(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}.
     *
     * <p>
     * Subclasses of this class may override this method to perform actions on this event or register a modification
     * action using {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onPreLinkageDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}.
     *
     * <p>
     * Subclasses of this class may override this method to perform actions on this event or register a modification
     * action using {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onLinkageDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}.
     *
     * <p>
     * Subclasses of this class may override this method to perform actions on this event or register a modification
     * action using {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onStatementDefinitionDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}.
     *
     * <p>
     * Subclasses of this class may override this method to perform actions on this event or register a modification
     * action using {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onFullDefinitionDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        final SubstatementValidator validator = getSubstatementValidator();
        if (validator != null) {
            validator.validate(stmt);
        }
    }

    @Override
    public boolean hasArgumentSpecificSupports() {
        // Most of statement supports don't have any argument specific supports, so return 'false'.
        return false;
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        // Most of statement supports don't have any argument specific supports, so return null.
        return null;
    }

    @Override
    public Optional<? extends Mutable<?, ?, ?>> copyAsChildOf(final Mutable<?, ?, ?> stmt,
            final Mutable<?, ?, ?> parent, final CopyType type, final QNameModule targetModule) {
        // FIXME: YANGTOOLS-694: filter out all context-independent substatements
        if (isContextIndependent() && stmt.allSubstatementsStream().findAny().isEmpty()) {
            // This statement is context-independent and has no substatements -- hence it can be freely shared.
            return Optional.of(stmt);
        }

        // FIXME: YANGTOOLS-694: this is still to eager, we really want to copy as a lazily-instantiated context,
        //                       so that we can support building an effective statement without copying anything --
        //                       we will typically end up not being inferred against. In that case, this slim contenxt
        //                       should end up dealing with differences at buildContext() time. This is a YANGTOOLS-1067
        //                       prerequisite (which will deal with what can and cannot be shared across instances).
        return Optional.of(parent.childCopyOf(stmt, type, targetModule));
    }

    /**
     * This statement is context-independent. This means its effective model is defined at the place of definition and
     * cannot change.
     *
     * <p>
     * Default implementation errs of the safe side by returning false.
     *
     * @return True if this statement's effective view is independent of the context where it is used and cannot be
     *         further modified by inference.
     */
    protected boolean isContextIndependent() {
        return false;
    }

    /**
     * Returns corresponding substatement validator of a statement support.
     *
     * @return substatement validator or null, if substatement validator is not defined
     */
    protected abstract @Nullable SubstatementValidator getSubstatementValidator();

}
