/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

public abstract class StatementSupport<A,D extends DeclaredStatement<A>,E extends EffectiveStatement<A, D>>
        implements StatementDefinition, StatementFactory<A,D,E> {

    private final StatementDefinition type;

    protected StatementSupport(StatementDefinition publicDefinition) {
        Preconditions.checkArgument(publicDefinition != this);
        this.type = Preconditions.checkNotNull(publicDefinition);
    }

    @Override
    public final QName getStatementName() {
        return type.getStatementName();
    }

    @Override
    public final QName getArgumentName() {
        return type.getArgumentName();
    }

    @Override
    public final Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return type.getDeclaredRepresentationClass();
    }

    @Override
    public final Class<? extends DeclaredStatement<?>> getEffectiveRepresentationClass() {
        return type.getEffectiveRepresentationClass();
    }

    public final StatementDefinition getPublicView() {
        return type;
    }

    public abstract A parseArgumentValue(StmtContext<?,?,?> parentCtx,String value);

    public void onStatementAdded(StmtContext.Mutable<A,D,E> stmt) {

    };

    public void onStatementDeclarationFinished(StmtContext.Mutable<A,D,E> stmt) {

    }

}
