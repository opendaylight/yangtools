/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

public abstract class ContextBuilder<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
    implements Builder<StatementContextBase<A, D, E>> {

    private final StatementDefinitionContext<A, D, E> definition;
    private final StatementSourceReference stmtRef;

    private StatementSourceReference argRef;
    private String rawArg;

    ContextBuilder(final StatementDefinitionContext<A, D, E> def, final StatementSourceReference sourceRef) {
        this.definition = Preconditions.checkNotNull(def);
        this.stmtRef = Preconditions.checkNotNull(sourceRef);
    }

    void setArgument(@Nonnull final String argument, @Nonnull final StatementSourceReference argumentSource) {
        SourceException.throwIf(!definition.hasArgument(), argumentSource, "Statement %s does not take argument",
            definition.getStatementName());
        this.rawArg = Preconditions.checkNotNull(argument);
        this.argRef = Preconditions.checkNotNull(argumentSource);
    }

    String getRawArgument() {
        return rawArg;
    }

    StatementSourceReference getStamementSource() {
        return stmtRef;
    }

    StatementSourceReference getArgumentSource() {
        return argRef;
    }

    StatementDefinitionContext<A, D, E> getDefinition() {
        return definition;
    }

    StatementIdentifier createIdentifier() {
        return new StatementIdentifier(definition.getStatementName(), rawArg);
    }

    /**
     * {@inheritDoc}
     *
     * @throws SourceException when a source-level problem is found
     */
    @Override
    public abstract StatementContextBase<A, D, E> build();
}
