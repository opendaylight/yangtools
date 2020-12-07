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

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent;

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
    private final @NonNull CopyPolicy copyPolicy;

    @Beta
    protected AbstractStatementSupport(final StatementDefinition publicDefinition, final CopyPolicy copyPolicy) {
        this.type = requireNonNull(publicDefinition);
        this.copyPolicy = requireNonNull(copyPolicy);
        checkArgument(publicDefinition != this);
    }

    @Override
    public final StatementDefinition getPublicView() {
        return type;
    }

    @Override
    public final CopyPolicy copyPolicy() {
        return copyPolicy;
    }

    @Override
    public final Current<A, D> effectiveCopyOf(final Current<A, D> stmt, final Parent parent, final CopyType copyType,
            final QNameModule targetModule) {
        switch (copyPolicy) {
            case CONTEXT_INDEPENDENT:
                return stmt;
            case DECLARED_COPY:
                return effectivelyEqual(stmt, parent, copyType, targetModule)
                    ? stmt : stmt.withParent(parent, copyType, targetModule);
            default:
                throw new VerifyException("Attempted to apply " + copyPolicy);
        }
    }

    protected boolean effectivelyEqual(final Current<A, D> stmt, final Parent parent, final CopyType copyType,
            final QNameModule targetModule) {
        return false;
    }

    //
    // FIXME: Are these useful?
    //
    protected static final boolean isSameHistory(final CopyHistory copyHistory, final CopyType copyType) {
        // FIXME: compare these ... how exactly? see what childCopyOf() does
        return false;
    }

    protected static final boolean isSameModule(final StmtContext<?, ?, ?> stmt, final QNameModule targetModule) {
        // FIXME: targetModule == null
        // FIXME: extract something from stmt (if we can?), for example QNameStatementSupport
        return false;
    }

    // Semantic comparison of parent
    protected static final boolean isSameParent(final StmtContext<?, ?, ?> parent, final StmtContext<?, ?, ?> newParent) {
        // TODO: This should never happen, I think. Perhaps an assertion is in order?
        return newParent.equals(newParent);
    }

    // FIXME: see ^^^

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

    /**
     * Returns corresponding substatement validator of a statement support.
     *
     * @return substatement validator or null, if substatement validator is not defined
     */
    protected abstract @Nullable SubstatementValidator getSubstatementValidator();
}
