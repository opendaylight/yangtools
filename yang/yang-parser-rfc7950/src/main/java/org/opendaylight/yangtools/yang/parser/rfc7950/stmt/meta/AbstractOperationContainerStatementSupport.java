/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Specialization of {@link AbstractQNameStatementSupport} for {@code input} and {@code output} statements.
 *
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
abstract class AbstractOperationContainerStatementSupport<D extends DeclaredStatement<QName>,
        E extends SchemaTreeEffectiveStatement<D>> extends AbstractImplicitStatementSupport<D, E> {
    private final Function<QNameModule, QName> createArgument;

    AbstractOperationContainerStatementSupport(final StatementDefinition publicDefinition,
            final YangParserConfiguration config, final Function<QNameModule, QName> createArgument) {
        super(publicDefinition, uninstantiatedPolicy(), config);
        this.createArgument = requireNonNull(createArgument);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return createArgument.apply(StmtContextUtils.getRootModuleQName(ctx));
    }

    @Override
    final @NonNull E copyDeclaredEffective(final Current<QName, D> stmt, final E original) {
        return copyDeclaredEffective(
            EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), original.effectiveSubstatements()),
            stmt, original);
    }

    abstract @NonNull E copyDeclaredEffective(int flags, @NonNull Current<QName, D> stmt,
        @NonNull E original);

    @Override
    final @NonNull E copyUndeclaredEffective(final Current<QName, D> stmt, final E original) {
        return copyUndeclaredEffective(
            EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), original.effectiveSubstatements()),
            stmt, original);
    }

    abstract @NonNull E copyUndeclaredEffective(int flags, @NonNull Current<QName, D> stmt, @NonNull E original);

    @Override
    final @NonNull E createDeclaredEffective(final Current<QName, D> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return createDeclaredEffective(
                EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), substatements), stmt, substatements);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    abstract @NonNull E createDeclaredEffective(int flags, @NonNull Current<QName, D> stmt,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);

    @Override
    final E createUndeclaredEffective(final Current<QName, D> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return createUndeclaredEffective(EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), substatements),
            stmt, substatements);
    }

    abstract @NonNull E createUndeclaredEffective(int flags, @NonNull Current<QName, D> stmt,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);
}
