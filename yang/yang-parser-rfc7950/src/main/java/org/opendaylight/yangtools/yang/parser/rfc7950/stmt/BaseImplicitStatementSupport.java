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
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * A massively-misnamed superclass for statements which are both schema tree participants and can be created as implicit
 * nodes. This covers {@code case}, {@code input} and {@code output} statements.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class BaseImplicitStatementSupport<D extends DeclaredStatement<QName>,
        E extends SchemaTreeEffectiveStatement<D>> extends BaseSchemaTreeStatementSupport<D, E> {
    protected BaseImplicitStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition);
    }

    @Override
    protected final E createEffective(
            final StmtContext<QName, D, E> ctx,
            final D declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final StatementSource source = ctx.getStatementSource();
        switch (ctx.getStatementSource()) {
            case CONTEXT:
                return createUndeclaredEffective(ctx, substatements);
            case DECLARATION:
                return createDeclaredEffective(ctx, substatements, declared);
            default:
                throw new IllegalStateException("Unhandled statement source " + source);
        }
    }

    @Override
    protected final E createEmptyEffective(final StmtContext<QName, D, E> ctx, final D declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }

    protected abstract @NonNull E createDeclaredEffective(@NonNull StmtContext<QName, D, E> ctx,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements, @NonNull D declared);

    protected abstract @NonNull E createUndeclaredEffective(@NonNull StmtContext<QName, D, E> ctx,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);

}
