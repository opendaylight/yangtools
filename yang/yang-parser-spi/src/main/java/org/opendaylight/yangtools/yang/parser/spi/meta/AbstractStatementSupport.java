/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;

/**
 * Baseline implementation class for common {@link StatementSupport} implementations. This class performs many of the
 * its duties in the canonical way -- taking away some amount of freedom for common functionality.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public abstract class AbstractStatementSupport<A, D extends DeclaredStatement<A>,
        E extends EffectiveStatement<A, D>> extends StatementSupport<A, D, E> {
    protected AbstractStatementSupport(final StatementDefinition publicDefinition, final StatementPolicy<A, D> policy) {
        super(publicDefinition, policy);
    }

    @Override
    public final D createDeclared(final StmtContext<A, D, ?> ctx) {
        final ImmutableList<? extends DeclaredStatement<?>> substatements = ctx.declaredSubstatements().stream()
                .map(StmtContext::declared)
                .collect(ImmutableList.toImmutableList());
        return substatements.isEmpty() ? createEmptyDeclared(ctx) : createDeclared(ctx, substatements);
    }

    protected abstract @NonNull D createDeclared(@NonNull StmtContext<A, D, ?> ctx,
            @NonNull ImmutableList<? extends DeclaredStatement<?>> substatements);

    protected abstract @NonNull D createEmptyDeclared(@NonNull StmtContext<A, D, ?> ctx);

    @Override
    public E createEffective(final Current<A, D> stmt,
            final Stream<? extends StmtContext<?, ?, ?>> declaredSubstatements,
            final Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements) {
        final ImmutableList<? extends EffectiveStatement<?, ?>> substatements =
                buildEffectiveSubstatements(stmt, statementsToBuild(stmt,
                    declaredSubstatements(declaredSubstatements, effectiveSubstatements)));
        return createEffective(stmt, substatements);
    }

    protected abstract @NonNull E createEffective(@NonNull Current<A, D> stmt,
            @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements);

    @Override
    public E copyEffective(final Current<A, D> stmt, final E original) {
        // Most implementations are only interested in substatements. copyOf() here should be a no-op
        return createEffective(stmt, ImmutableList.copyOf(original.effectiveSubstatements()));
    }

    /**
     * Give statement support a hook to transform statement contexts before they are built. Default implementation
     * does nothing, but note {@code augment} statement performs a real transformation.
     *
     * @param ctx Effective capture of this statement's significant state
     * @param substatements Substatement contexts which have been determined to be built
     * @return Substatement context which are to be actually built
     */
    protected List<? extends StmtContext<?, ?, ?>> statementsToBuild(final Current<A, D> ctx,
            final List<? extends StmtContext<?, ?, ?>> substatements) {
        return substatements;
    }

    // FIXME: add documentation
    public static final <E extends EffectiveStatement<?, ?>> @Nullable E findFirstStatement(
            final Collection<? extends EffectiveStatement<?, ?>> statements, final Class<E> type) {
        for (EffectiveStatement<?, ?> stmt : statements) {
            if (type.isInstance(stmt)) {
                return type.cast(stmt);
            }
        }
        return null;
    }

    // FIXME: add documentation
    public static final <A, E extends EffectiveStatement<A, ?>> A findFirstArgument(
            final Collection<? extends EffectiveStatement<?, ?>> statements, final Class<@NonNull E> type,
                    final A defValue) {
        final @Nullable E stmt = findFirstStatement(statements, type);
        return stmt != null ? stmt.argument() : defValue;
    }

    /**
     * Create a set of substatements. This method is split out so it can be overridden in subclasses adjust the
     * resulting statements.
     *
     * @param stmt Current statement context
     * @param substatements proposed substatements
     * @return Built effective substatements
     */
    protected @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> buildEffectiveSubstatements(
            final Current<A, D> stmt, final List<? extends StmtContext<?, ?, ?>> substatements) {
        return defaultBuildEffectiveSubstatements(substatements);
    }

    private static @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> defaultBuildEffectiveSubstatements(
            final List<? extends StmtContext<?, ?, ?>> substatements) {
        return substatements.stream()
                .filter(StmtContext::isSupportedToBuildEffective)
                .map(StmtContext::buildEffective)
                .collect(ImmutableList.toImmutableList());
    }

    private static @NonNull List<StmtContext<?, ?, ?>> declaredSubstatements(
            final Stream<? extends StmtContext<?, ?, ?>> declaredSubstatements,
            final Stream<? extends StmtContext<?, ?, ?>> effectiveSubstatements) {
        /*
         * This dance is required to ensure that effects of 'uses' nodes are applied in the same order as
         * the statements were defined -- i.e. if we have something like this:
         *
         * container foo {
         *   uses bar;
         *   uses baz;
         * }
         *
         * grouping baz {
         *   leaf baz {
         *     type string;
         *   }
         * }
         *
         * grouping bar {
         *   leaf bar {
         *     type string;
         *   }
         * }
         *
         * The reactor would first inline 'uses baz' as that definition is the first one completely resolved and then
         * inline 'uses bar'. Here we are iterating in declaration order re-inline the statements.
         *
         * FIXME: 7.0.0: this really should be handled by UsesStatementSupport such that 'uses baz' would have a
         *               prerequisite of a resolved 'uses bar'.
         */
        final List<StmtContext<?, ?, ?>> declaredInit = declaredSubstatements
            .filter(StmtContext::isSupportedByFeatures)
            .collect(Collectors.toList());

        final List<StmtContext<?, ?, ?>> substatementsInit = new ArrayList<>();
        Set<StmtContext<?, ?, ?>> filteredStatements = null;
        for (final StmtContext<?, ?, ?> declaredSubstatement : declaredInit) {
            substatementsInit.add(declaredSubstatement);

            // FIXME: YANGTOOLS-1161: we need to integrate this functionality into the reactor, so that this
            //                        transformation is something reactor's declared statements already take into
            //                        account.
            final Collection<? extends StmtContext<?, ?, ?>> effect = declaredSubstatement.getEffectOfStatement();
            if (!effect.isEmpty()) {
                if (filteredStatements == null) {
                    filteredStatements = new HashSet<>();
                }
                filteredStatements.addAll(effect);
                substatementsInit.addAll(effect);
            }
        }

        final Stream<? extends StmtContext<?, ?, ?>> effective;
        if (filteredStatements != null) {
            final Set<StmtContext<?, ?, ?>> filtered = filteredStatements;
            effective = effectiveSubstatements.filter(stmt -> !filtered.contains(stmt));
        } else {
            effective = effectiveSubstatements;
        }

        substatementsInit.addAll(effective.collect(Collectors.toList()));
        return substatementsInit;
    }
}
