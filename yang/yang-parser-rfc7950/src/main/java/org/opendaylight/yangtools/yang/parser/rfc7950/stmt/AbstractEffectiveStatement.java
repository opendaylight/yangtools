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
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class AbstractEffectiveStatement<A, D extends DeclaredStatement<A>>
        implements EffectiveStatement<A, D> {
    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends V> get(final Class<N> namespace,
            final K identifier) {
        return Optional.ofNullable(getAll(namespace).get(requireNonNull(identifier)));
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        final Optional<? extends Map<K, V>> ret = getNamespaceContents(requireNonNull(namespace));
        return ret.isPresent() ? ret.get() : ImmutableMap.of();
    }

    @Override
    public @Nullable A argument() {
        return getDeclared().argument();
    }

    /**
     * Return the statement-specific contents of specified namespace, if available.
     *
     * @param namespace Requested namespace
     * @return Namespace contents, if available.
     */
    protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final @NonNull Class<N> namespace) {
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public final <T> Collection<T> allSubstatementsOfType(final Class<T> type) {
        return Collection.class.cast(Collections2.filter(effectiveSubstatements(), type::isInstance));
    }

    protected final <T> @Nullable T firstSubstatementOfType(final Class<T> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).findFirst().map(type::cast).orElse(null);
    }

    protected final <R> R firstSubstatementOfType(final Class<?> type, final Class<R> returnType) {
        return effectiveSubstatements().stream()
                .filter(((Predicate<Object>)type::isInstance).and(returnType::isInstance))
                .findFirst().map(returnType::cast).orElse(null);
    }

    protected final EffectiveStatement<?, ?> firstEffectiveSubstatementOfType(final Class<?> type) {
        return effectiveSubstatements().stream().filter(type::isInstance).findFirst().orElse(null);
    }

    /**
     * Create a set of substatements. This method is split out so it can be overridden in
     * ExtensionEffectiveStatementImpl to leak a not-fully-initialized instance.
     *
     * @param substatements proposed substatements
     * @return Filtered substatements
     */
    public static ImmutableList<? extends EffectiveStatement<?, ?>> buildEffectiveSubstatements(
            final List<? extends StmtContext<?, ?, ?>> substatements) {
        return substatements.stream()
                .filter(StmtContext::isSupportedToBuildEffective)
                .map(StmtContext::buildEffective)
                .collect(ImmutableList.toImmutableList());
    }

    public static final ImmutableList<? extends EffectiveStatement<?, ?>> buildEffectiveSubstatements(
            final StmtContext<?, ?, ?> ctx) {
        return buildEffectiveSubstatements(declaredSubstatements(ctx));
    }

    public static final @NonNull List<StmtContext<?, ?, ?>> declaredSubstatements(final StmtContext<?, ?, ?> ctx) {
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
         * FIXME: 5.0.0: this really should be handled by UsesStatementSupport such that 'uses baz' would have a
         *               prerequisite of a resolved 'uses bar'.
         */
        final List<StmtContext<?, ?, ?>> substatementsInit = new ArrayList<>();
        Set<StmtContext<?, ?, ?>> filteredStatements = null;
        for (final StmtContext<?, ?, ?> declaredSubstatement : ctx.declaredSubstatements()) {
            if (declaredSubstatement.isSupportedByFeatures()) {
                substatementsInit.add(declaredSubstatement);

                final Collection<? extends StmtContext<?, ?, ?>> effect = declaredSubstatement.getEffectOfStatement();
                if (!effect.isEmpty()) {
                    if (filteredStatements == null) {
                        filteredStatements = new HashSet<>();
                    }
                    filteredStatements.addAll(effect);
                    substatementsInit.addAll(effect);
                }
            }
        }

        if (filteredStatements != null) {
            for (StmtContext<?, ?, ?> stmt : ctx.effectiveSubstatements()) {
                if (!filteredStatements.contains(stmt)) {
                    substatementsInit.add(stmt);
                }
            }
        } else {
            substatementsInit.addAll(ctx.effectiveSubstatements());
        }

        return substatementsInit;
    }
}
