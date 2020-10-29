/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Specialization of {@link BaseQNameStatementSupport} for {@code input} and {@code output} statements.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class BaseOperationContainerStatementSupport<D extends DeclaredStatement<QName>,
        E extends SchemaTreeEffectiveStatement<D>> extends BaseImplicitStatementSupport<D, E> {
    protected BaseOperationContainerStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition);
    }

    @Override
    protected final @NonNull E createDeclaredEffective(final StmtContext<QName, D, E> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements, final D declared) {
        return createDeclaredEffective(historyAndStatusFlags(ctx, substatements), ctx, substatements, declared);
    }

    protected abstract @NonNull E createDeclaredEffective(int flags, @NonNull StmtContext<QName, D, E> ctx,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements, @NonNull D declared);

    @Override
    protected final E createUndeclaredEffective(final StmtContext<QName, D, E> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return createUndeclaredEffective(historyAndStatusFlags(ctx, substatements), ctx, substatements);
    }

    protected abstract @NonNull E createUndeclaredEffective(int flags, @NonNull StmtContext<QName, D, E> ctx,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);
}
