/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.LinkedList;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import java.util.Collections;
import java.util.NoSuchElementException;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import com.google.common.collect.Collections2;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import com.google.common.collect.FluentIterable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import java.util.Collection;
import java.util.Map;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

abstract public class EffectiveStatementBase<A, D extends DeclaredStatement<A>>
        implements EffectiveStatement<A, D> {

    private final StmtContext<A, D, ?> stmtCtx;
    private final ImmutableList<? extends EffectiveStatement<?, ?>> substatements;
    private final StatementSource statementSource;
    private final StatementDefinition statementDefinition;
    private D declaredInstance;

    private final A argument;

    public EffectiveStatementBase(StmtContext<A, D, ?> ctx) {

        this.stmtCtx = ctx;
        this.statementDefinition = ctx.getPublicDefinition();
        this.argument = ctx.getStatementArgument();
        this.statementSource = ctx.getStatementSource();

        Collection<StatementContextBase<?, ?, ?>> declaredSubstatements = ctx
                .declaredSubstatements();
        Collection<StatementContextBase<?, ?, ?>> effectiveSubstatements = ctx
                .effectiveSubstatements();

        Collection<StatementContextBase<?, ?, ?>> substatementsInit = new LinkedList<>();

        for (StatementContextBase<?, ?, ?> declaredSubstatement : declaredSubstatements) {
            if (declaredSubstatement.getPublicDefinition() == Rfc6020Mapping.USES) {
                substatementsInit.add(declaredSubstatement);
                substatementsInit.addAll(declaredSubstatement
                        .getEffectOfStatement());
                effectiveSubstatements.removeAll(declaredSubstatement
                        .getEffectOfStatement());
            } else {
                substatementsInit.add(declaredSubstatement);
            }
        }

        substatementsInit.addAll(effectiveSubstatements);

        this.substatements = FluentIterable.from(substatementsInit)
                .filter(StmtContextUtils.IS_SUPPORTED_TO_BUILD_EFFECTIVE)
                .transform(StmtContextUtils.buildEffective()).toList();
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
        if (declaredInstance == null) {
            declaredInstance = stmtCtx.buildDeclared();
        }
        return declaredInstance;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V get(
            Class<N> namespace, K identifier) {
        return stmtCtx.getFromNamespace(namespace, identifier);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(
            Class<N> namespace) {
        return (Map<K, V>) stmtCtx.getAllFromNamespace(namespace);
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return substatements;
    }

    public StmtContext<A, D, ?> getStatementContext() {
        return stmtCtx;
    }

    protected final <S extends EffectiveStatement<?, ?>> S firstEffective(
            Class<S> type) {
        S result = null;
        try {
            result = type.cast(Iterables.find(substatements,
                    Predicates.instanceOf(type)));
        } catch (NoSuchElementException e) {
            result = null;
        }
        return result;
    }

    protected final <S extends SchemaNode> S firstSchemaNode(Class<S> type) {
        S result = null;
        try {
            result = type.cast(Iterables.find(substatements,
                    Predicates.instanceOf(type)));
        } catch (NoSuchElementException e) {
            result = null;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    protected final <T> Collection<T> allSubstatementsOfType(Class<T> type) {
        Collection<T> result = null;

        try {
            result = Collection.class.cast(Collections2.filter(substatements,
                    Predicates.instanceOf(type)));
        } catch (NoSuchElementException e) {
            result = Collections.emptyList();
        }
        return result;
    }

    protected final <T> T firstSubstatementOfType(Class<T> type) {
        T result = null;
        try {
            result = type.cast(Iterables.find(substatements,
                    Predicates.instanceOf(type)));
        } catch (NoSuchElementException e) {
            result = null;
        }
        return result;
    }

    protected final <R> R firstSubstatementOfType(Class<?> type,
            Class<R> returnType) {
        R result = null;
        try {
            result = returnType.cast(Iterables.find(
                    substatements,
                    Predicates.and(Predicates.instanceOf(type),
                            Predicates.instanceOf(returnType))));
        } catch (NoSuchElementException e) {
            result = null;
        }
        return result;
    }

}
