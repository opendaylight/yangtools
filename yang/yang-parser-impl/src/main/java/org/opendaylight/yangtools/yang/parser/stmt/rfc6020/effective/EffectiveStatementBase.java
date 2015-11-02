/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Optional;
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
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

public abstract class EffectiveStatementBase<A, D extends DeclaredStatement<A>> implements EffectiveStatement<A, D> {

    private final List<? extends EffectiveStatement<?, ?>> substatements;
    private final StatementSource statementSource;
    private final StatementDefinition statementDefinition;
    private final A argument;
    private final D declaredInstance;

    public EffectiveStatementBase(final StmtContext<A, D, ?> ctx) {
        this.statementDefinition = ctx.getPublicDefinition();
        this.argument = ctx.getStatementArgument();
        this.statementSource = ctx.getStatementSource();

        Collection<StatementContextBase<?, ?, ?>> effectiveSubstatements = ctx.effectiveSubstatements();

        Collection<StatementContextBase<?, ?, ?>> substatementsInit = new ArrayList<>();

        for(StatementContextBase<?, ?, ?> declaredSubstatement : ctx.declaredSubstatements()) {
            if (declaredSubstatement.getPublicDefinition().equals(Rfc6020Mapping.USES)) {
                substatementsInit.add(declaredSubstatement);
                substatementsInit.addAll(declaredSubstatement.getEffectOfStatement());
                ((StatementContextBase<?, ?, ?>)ctx).removeStatementsFromEffectiveSubstatements(declaredSubstatement
                        .getEffectOfStatement());
            } else {
                substatementsInit.add(declaredSubstatement);
            }
        }

        substatementsInit.addAll(effectiveSubstatements);

        this.substatements = ImmutableList.copyOf(Collections2.transform(
            Collections2.filter(substatementsInit, StmtContextUtils.IS_SUPPORTED_TO_BUILD_EFFECTIVE),
                StmtContextUtils.buildEffective()));
        declaredInstance = ctx.buildDeclared();
    }

    @Override
    public StatementDefinition statementDefinition() {
        return statementDefinition;
    }

    @Override
    public A argument() {
        return argument;
    }

    @Override
    public StatementSource getStatementSource() {
        return statementSource;
    }

    @Override
    public D getDeclared() {
        return declaredInstance;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }

    protected final <S extends EffectiveStatement<?, ?>> S firstEffective(final Class<S> type) {
        Optional<? extends EffectiveStatement<?, ?>> possible = Iterables.tryFind(substatements,
                Predicates.instanceOf(type));
        return possible.isPresent() ? type.cast(possible.get()) : null;
    }

    protected final <S extends SchemaNode> S firstSchemaNode(final Class<S> type) {
        Optional<? extends EffectiveStatement<?, ?>> possible = Iterables.tryFind(substatements,
                Predicates.instanceOf(type));
        return possible.isPresent() ? type.cast(possible.get()) : null;
    }

    @SuppressWarnings("unchecked")
    protected final <T> Collection<T> allSubstatementsOfType(final Class<T> type) {
        return Collection.class.cast(Collections2.filter(substatements, Predicates.instanceOf(type)));
    }

    protected final <T> T firstSubstatementOfType(final Class<T> type) {
        Optional<? extends EffectiveStatement<?, ?>> possible = Iterables.tryFind(substatements,
                Predicates.instanceOf(type));
        return possible.isPresent() ? type.cast(possible.get()) : null;
    }

    protected final <R> R firstSubstatementOfType(final Class<?> type, final Class<R> returnType) {
        Optional<? extends EffectiveStatement<?, ?>> possible = Iterables.tryFind(substatements,
                Predicates.and(Predicates.instanceOf(type), Predicates.instanceOf(returnType)));
        return possible.isPresent() ? returnType.cast(possible.get()) : null;
    }

    protected final EffectiveStatement<?, ?> firstEffectiveSubstatementOfType(final Class<?> type) {
        Optional<? extends EffectiveStatement<?, ?>> possible = Iterables.tryFind(substatements,
                Predicates.instanceOf(type));
        return possible.isPresent() ? possible.get() : null;
    }
}
