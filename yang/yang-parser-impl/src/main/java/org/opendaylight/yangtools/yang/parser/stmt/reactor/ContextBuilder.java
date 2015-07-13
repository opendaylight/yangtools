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
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;

abstract class ContextBuilder<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>> {

    private final StatementDefinitionContext<A, D, E> definition;
    private final StatementSourceReference stmtRef;
    private String rawArg;
    private StatementSourceReference argRef;

    public ContextBuilder(StatementDefinitionContext<A, D, E> def, StatementSourceReference sourceRef) {
        this.definition = def;
        this.stmtRef = sourceRef;
    }

    public void setArgument(@Nonnull String argument, @Nonnull StatementSourceReference argumentSource) {
        Preconditions.checkArgument(definition.hasArgument(), "Statement does not take argument.");
        this.rawArg = Preconditions.checkNotNull(argument);
        this.argRef = Preconditions.checkNotNull(argumentSource);
    }

    public String getRawArgument() {
        return rawArg;
    }

    public StatementSourceReference getStamementSource() {
        return stmtRef;
    }

    public StatementSourceReference getArgumentSource() {
        return argRef;
    }

    public StatementDefinitionContext<A, D, E> getDefinition() {
        return definition;
    }

    public StatementIdentifier createIdentifier() {
        return new StatementIdentifier(definition.getStatementName(), rawArg);
    }

    public abstract StatementContextBase<A, D, E> build() throws SourceException;

}
