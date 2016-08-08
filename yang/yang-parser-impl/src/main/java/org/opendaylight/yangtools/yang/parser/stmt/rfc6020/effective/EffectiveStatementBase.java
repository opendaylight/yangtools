/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public abstract class EffectiveStatementBase<A, D extends DeclaredStatement<A>> implements EffectiveStatement<A, D> {

    private static final Predicate<StmtContext<?, ?, ?>> IS_SUPPORTED_TO_BUILD_EFFECTIVE = new Predicate<StmtContext<?, ?, ?>>() {
        @Override
        public boolean apply(final StmtContext<?, ?, ?> input) {
            return input.isSupportedToBuildEffective();
        }
    };

    private static final Predicate<StmtContext<?, ?, ?>> IS_UNKNOWN_STATEMENT_CONTEXT = new Predicate<StmtContext<?, ?, ?>>() {
        @Override
        public boolean apply(final StmtContext<?, ?, ?> input) {
            return StmtContextUtils.isUnknownStatement(input);
        }
    };

    private static final Predicate<StatementContextBase<?, ?, ?>> ARE_FEATURES_SUPPORTED = new Predicate<StatementContextBase<?, ?, ?>>() {

        @Override
        public boolean apply(final StatementContextBase<?, ?, ?> input) {
            return StmtContextUtils.areFeaturesSupported(input);
        }
    };

    private final List<? extends EffectiveStatement<?, ?>> substatements;
    private final List<StatementContextBase<?, ?, ?>> unknownSubstatementsToBuild;

    protected EffectiveStatementBase(final StmtContext<A, D, ?> ctx) {
        this(ctx, true);
    }

    /**
     * Constructor.
     *
     * @param ctx
     *            context of statement.
     * @param buildUnknownSubstatements
     *            if it is false, the unknown substatements are omitted from
     *            build of effective substatements till the call of either
     *            effectiveSubstatements or getOmittedUnknownSubstatements
     *            method. The main purpose of this is to allow the build of
     *            recursive extension definitions.
     */
    protected EffectiveStatementBase(final StmtContext<A, D, ?> ctx, final boolean buildUnknownSubstatements) {

        final Collection<StatementContextBase<?, ?, ?>> effectiveSubstatements = ctx.effectiveSubstatements();
        final Collection<StatementContextBase<?, ?, ?>> substatementsInit = new ArrayList<>();

        final Collection<StatementContextBase<?, ?, ?>> supportedDeclaredSubStmts = Collections2.filter(
                ctx.declaredSubstatements(), ARE_FEATURES_SUPPORTED);
        for (final StatementContextBase<?, ?, ?> declaredSubstatement : supportedDeclaredSubStmts) {
            if (declaredSubstatement.getPublicDefinition().equals(Rfc6020Mapping.USES)) {
                substatementsInit.add(declaredSubstatement);
                substatementsInit.addAll(declaredSubstatement.getEffectOfStatement());
                ((StatementContextBase<?, ?, ?>) ctx).removeStatementsFromEffectiveSubstatements(declaredSubstatement
                        .getEffectOfStatement());
            } else {
                substatementsInit.add(declaredSubstatement);
            }
        }
        substatementsInit.addAll(effectiveSubstatements);

        Collection<StatementContextBase<?, ?, ?>> substatementsToBuild = Collections2.filter(substatementsInit,
                IS_SUPPORTED_TO_BUILD_EFFECTIVE);
        if (!buildUnknownSubstatements) {
            this.unknownSubstatementsToBuild = ImmutableList.copyOf(Collections2.filter(substatementsToBuild,
                    IS_UNKNOWN_STATEMENT_CONTEXT));
            substatementsToBuild = Collections2.filter(substatementsToBuild,
                    Predicates.not(IS_UNKNOWN_STATEMENT_CONTEXT));
        } else {
            this.unknownSubstatementsToBuild = ImmutableList.of();
        }

        this.substatements = ImmutableList.copyOf(Collections2.transform(substatementsToBuild, StatementContextBase::buildEffective));
    }

    Collection<EffectiveStatement<?, ?>> getOmittedUnknownSubstatements() {
        return Collections2.transform(unknownSubstatementsToBuild, StatementContextBase::buildEffective);
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public final <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public final Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        if (unknownSubstatementsToBuild.isEmpty()) {
            return substatements;
        } else {
            return ImmutableList.copyOf(Iterables.concat(substatements, getOmittedUnknownSubstatements()));
        }
    }

    protected final <S extends EffectiveStatement<?, ?>> S firstEffective(final Class<S> type) {
        final Optional<? extends EffectiveStatement<?, ?>> possible = Iterables.tryFind(substatements,
                Predicates.instanceOf(type));
        return possible.isPresent() ? type.cast(possible.get()) : null;
    }

    protected final <S extends SchemaNode> S firstSchemaNode(final Class<S> type) {
        final Optional<? extends EffectiveStatement<?, ?>> possible = Iterables.tryFind(substatements,
                Predicates.instanceOf(type));
        return possible.isPresent() ? type.cast(possible.get()) : null;
    }

    @SuppressWarnings("unchecked")
    protected final <T> Collection<T> allSubstatementsOfType(final Class<T> type) {
        return Collection.class.cast(Collections2.filter(substatements, Predicates.instanceOf(type)));
    }

    protected final <T> T firstSubstatementOfType(final Class<T> type) {
        final Optional<? extends EffectiveStatement<?, ?>> possible = Iterables.tryFind(substatements,
                Predicates.instanceOf(type));
        return possible.isPresent() ? type.cast(possible.get()) : null;
    }

    protected final <R> R firstSubstatementOfType(final Class<?> type, final Class<R> returnType) {
        final Optional<? extends EffectiveStatement<?, ?>> possible = Iterables.tryFind(substatements,
                Predicates.and(Predicates.instanceOf(type), Predicates.instanceOf(returnType)));
        return possible.isPresent() ? returnType.cast(possible.get()) : null;
    }

    protected final EffectiveStatement<?, ?> firstEffectiveSubstatementOfType(final Class<?> type) {
        return Iterables.tryFind(substatements, Predicates.instanceOf(type)).orNull();
    }
}
