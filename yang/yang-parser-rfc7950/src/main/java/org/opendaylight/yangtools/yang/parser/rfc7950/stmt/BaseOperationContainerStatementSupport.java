/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Specialization of {@link BaseQNameStatementSupport} for {@code input} and {@code output} statements.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class BaseOperationContainerStatementSupport<D extends DeclaredStatement<QName>,
        E extends SchemaTreeEffectiveStatement<D>> extends BaseImplicitStatementSupport<D, E> {
    private final Function<QNameModule, QName> createArgument;

    @Deprecated
    protected BaseOperationContainerStatementSupport(final StatementDefinition publicDefinition,
            final Function<QNameModule, QName> createArgument, final CopyPolicy copyPolicy) {
        super(publicDefinition, copyPolicy);
        this.createArgument = requireNonNull(createArgument);
    }

    protected BaseOperationContainerStatementSupport(final StatementDefinition publicDefinition,
            final Function<QNameModule, QName> createArgument, final StatementPolicy<QName, D> policy) {
        super(publicDefinition, policy);
        this.createArgument = requireNonNull(createArgument);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return createArgument.apply(StmtContextUtils.getRootModuleQName(ctx));
    }

    @Override
    protected final @NonNull E createDeclaredEffective(final Current<QName, D> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return createDeclaredEffective(historyAndStatusFlags(stmt.history(), substatements), stmt, substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    protected abstract @NonNull E createDeclaredEffective(int flags, @NonNull Current<QName, D> stmt,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);

    @Override
    protected final E createUndeclaredEffective(final Current<QName, D> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return createUndeclaredEffective(historyAndStatusFlags(stmt.history(), substatements), stmt, substatements);
    }

    protected abstract @NonNull E createUndeclaredEffective(int flags, @NonNull Current<QName, D> stmt,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);
}
