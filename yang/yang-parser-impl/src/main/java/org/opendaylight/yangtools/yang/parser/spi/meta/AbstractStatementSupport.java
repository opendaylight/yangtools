/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Class providing necessary support for processing a YANG statement.
 *
 * This class is intended to be subclassed by developers, who want to
 * introduce support of statement to parser.
 *
 * @param <A>
 *            Argument type
 * @param <D>
 *            Declared Statement representation
 * @param <E>
 *            Effective Statement representation
 */
public abstract class AbstractStatementSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        implements StatementDefinition, StatementFactory<A, D, E>, StatementSupport<A, D, E> {

    private final StatementDefinition type;

    protected AbstractStatementSupport(final StatementDefinition publicDefinition) {
        Preconditions.checkArgument(publicDefinition != this);
        this.type = Preconditions.checkNotNull(publicDefinition);
    }

    @Nonnull
    @Override
    public final QName getStatementName() {
        return type.getStatementName();
    }

    @Override
    public final QName getArgumentName() {
        return type.getArgumentName();
    }

    @Nonnull
    @Override
    public final Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return type.getDeclaredRepresentationClass();
    }

    @Nonnull
    @Override
    public final Class<? extends EffectiveStatement<?,?>> getEffectiveRepresentationClass() {
        return type.getEffectiveRepresentationClass();
    }

    @Override
    public final StatementDefinition getPublicView() {
        return type;
    }

    @Override
    public Optional<StatementSupport<?, ?, ?>> getImplicitParentFor(final StatementDefinition stmtDef) {
        // NOOP for most implementations and also no implicit parent
        return Optional.empty();
    }

    @Override
    public void onStatementAdded(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}
     *
     * Subclasses of this class may override this method to perform actions on
     * this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onPreLinkageDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}
     *
     * Subclasses of this class may override this method to perform actions on
     * this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onLinkageDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}
     *
     * Subclasses of this class may override this method to perform actions on
     * this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onStatementDefinitionDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     * {@inheritDoc}
     *
     * Subclasses of this class may override this method to perform actions on
     * this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     */
    @Override
    public void onFullDefinitionDeclared(final StmtContext.Mutable<A, D, E> stmt) {
        final SubstatementValidator validator = getSubstatementValidator();
        if (validator != null) {
            validator.validate(stmt);
        }
    }

    @Override
    public boolean isArgumentYinElement() {
        return getPublicView().isArgumentYinElement();
    }

    @Override
    public boolean hasArgumentSpecificSupports() {
        // Most of statement supports don't have any argument specific
        // supports, so return 'false'.
        return false;
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        // Most of statement supports don't have any argument specific
        // supports, so return null.
        return null;
    }

    /**
     * Returns corresponding substatement validator of a statement support
     *
     * @return substatement validator or null, if substatement validator is not
     *         defined
     */
    @Nullable
    protected abstract SubstatementValidator getSubstatementValidator();
}
