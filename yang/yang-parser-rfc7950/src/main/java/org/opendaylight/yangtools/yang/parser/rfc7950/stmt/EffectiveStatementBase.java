/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public abstract class EffectiveStatementBase<A, D extends DeclaredStatement<A>> implements EffectiveStatement<A, D> {
    private final @NonNull ImmutableList<? extends EffectiveStatement<?, ?>> substatements;

    /**
     * Constructor.
     *
     * @param ctx context of statement.
     */
    protected EffectiveStatementBase(final StmtContext<A, D, ?> ctx) {
        final Collection<? extends StmtContext<?, ?, ?>> effectiveSubstatements = ctx.effectiveSubstatements();
        final Collection<StmtContext<?, ?, ?>> substatementsInit = new ArrayList<>();

        final Collection<? extends StmtContext<?, ?, ?>> supportedDeclaredSubStmts = Collections2.filter(
                ctx.declaredSubstatements(), StmtContext::isSupportedByFeatures);
        for (final StmtContext<?, ?, ?> declaredSubstatement : supportedDeclaredSubStmts) {
            if (YangStmtMapping.USES == declaredSubstatement.getPublicDefinition()) {
                substatementsInit.add(declaredSubstatement);
                substatementsInit.addAll(declaredSubstatement.getEffectOfStatement());
                ((StatementContextBase<?, ?, ?>) ctx).removeStatementsFromEffectiveSubstatements(declaredSubstatement
                        .getEffectOfStatement());
            } else {
                substatementsInit.add(declaredSubstatement);
            }
        }
        substatementsInit.addAll(effectiveSubstatements);

        this.substatements = ImmutableList.copyOf(initSubstatements(substatementsInit));
    }

    /**
     * Create a set of substatements. This method is split out so it can be overridden in
     * ExtensionEffectiveStatementImpl to leak a not-fully-initialized instance.
     *
     * @param substatementsInit proposed substatements
     * @return Filtered substatements
     */
    protected Collection<? extends EffectiveStatement<?, ?>> initSubstatements(
            final Collection<? extends StmtContext<?, ?, ?>> substatementsInit) {
        return Collections2.transform(Collections2.filter(substatementsInit,
            StmtContext::isSupportedToBuildEffective), StmtContext::buildEffective);
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        return findAll(namespace).get(requireNonNull(identifier));
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        return getNamespaceContents(requireNonNull(namespace)).orElse(null);
    }

    /**
     * Return the statement-specific contents of specified namespace, if available.
     *
     * @param namespace Requested namespace
     * @return Namespace contents, if available.
     */
    @Beta
    protected <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends Map<K, V>> getNamespaceContents(
            final @NonNull Class<N> namespace) {
        return Optional.empty();
    }

    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }

    /**
     * Find first substatement of specified type.
     *
     * @param type Requested type
     * @return First matching substatement, or null if no match is found.
     *
     * @deprecated Use {@link #findFirstEffectiveSubstatement(Class)} instead.
     */
    @Deprecated
    public final <S extends EffectiveStatement<?, ?>> S firstEffective(final Class<S> type) {
        return findFirstEffectiveSubstatement(type).orElse(null);
    }

    protected final <S extends SchemaNode> S firstSchemaNode(final Class<S> type) {
        return substatements.stream().filter(type::isInstance).findFirst().map(type::cast).orElse(null);
    }

    @SuppressWarnings("unchecked")
    public final <T> Collection<T> allSubstatementsOfType(final Class<T> type) {
        return Collection.class.cast(Collections2.filter(substatements, type::isInstance));
    }

    protected final <T> @Nullable T firstSubstatementOfType(final Class<T> type) {
        return substatements.stream().filter(type::isInstance).findFirst().map(type::cast).orElse(null);
    }

    protected final <R> R firstSubstatementOfType(final Class<?> type, final Class<R> returnType) {
        return substatements.stream()
                .filter(((Predicate<Object>)type::isInstance).and(returnType::isInstance))
                .findFirst().map(returnType::cast).orElse(null);
    }

    protected final EffectiveStatement<?, ?> firstEffectiveSubstatementOfType(final Class<?> type) {
        return substatements.stream().filter(type::isInstance).findFirst().orElse(null);
    }
}
