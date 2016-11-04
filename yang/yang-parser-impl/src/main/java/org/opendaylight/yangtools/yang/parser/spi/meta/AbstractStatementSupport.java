/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 *
 * Class providing necessary support for processing YANG statement.
 *
 * This class is intended to be subclassed by developers, which want to
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

    protected AbstractStatementSupport(StatementDefinition publicDefinition) {
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
    public abstract A parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) throws SourceException;

    @Override
    public void onStatementAdded(StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     *
     * {@inheritDoc}
     *
     * Subclasses of this class may override this method to perform actions on
     * this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     */
    @Override
    public void onPreLinkageDeclared(StmtContext.Mutable<A, D, E> stmt) {
        // NOOP for most implementations
    }

    /**
     *
     * {@inheritDoc}
     *
     * Subclasses of this class may override this method to perform actions on
     * this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     */
    @Override
    public void onLinkageDeclared(StmtContext.Mutable<A, D, E> stmt) throws SourceException {
        // NOOP for most implementations
    }

    /**
     *
     * {@inheritDoc}
     *
     * Subclasses of this class may override this method to perform actions on
     * this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     */
    @Override
    public void onStatementDefinitionDeclared(StmtContext.Mutable<A, D, E> stmt) throws SourceException {
        // NOOP for most implementations
    }

    /**
     *
     * {@inheritDoc}
     *
     * Subclasses of this class may override this method to perform actions on
     * this event or register modification action using
     * {@link StmtContext.Mutable#newInferenceAction(ModelProcessingPhase)}.
     *
     */
    @Override
    public void onFullDefinitionDeclared(StmtContext.Mutable<A, D, E> stmt) throws SourceException {
        // NOOP for most implementations
    }

    @Override
    public boolean isArgumentYinElement() {
        return getPublicView().isArgumentYinElement();
    }
}
