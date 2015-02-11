/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;


public abstract class AbstractDeclaredStatement<A> implements DeclaredStatement<A> {

    private static final Function<StmtContext<?, ?,?>, DeclaredStatement<?>> BUILD_DECLARED = new Function<StmtContext<?,?,?>, DeclaredStatement<?>>() {

        @Override
        public DeclaredStatement<?> apply(StmtContext<?,?,?> input) {
            return input.buildDeclared();
        }

    };

    private final A argument;
    private final String rawArgument;
    private final Iterable<? extends DeclaredStatement<?>> substatements;
    private final StatementDefinition definition;
    private final StatementSource source;

    protected AbstractDeclaredStatement(StmtContext<A,?,?> context) {
        rawArgument = context.rawStatementArgument();
        argument = context.getStatementArgument();
        source = context.getStatementSource();
        definition = context.getPublicDefinition();
        substatements = FluentIterable.from(context.getDeclaredSubstatements()).transform(BUILD_DECLARED).toList();
    }

    @SuppressWarnings("unchecked")
    protected final <S extends DeclaredStatement<?>> S firstDeclared(Class<S> type) {
        return (S) Iterables.find(substatements, Predicates.instanceOf(type));
    }

    @Override
    public String rawArgument() {
        return rawArgument;
    }

    @Override
    public A argument() {
        return argument;
    }

    @Override
    public StatementDefinition statementDefinition() {
        return definition;
    }

    @Override
    public Iterable<? extends DeclaredStatement<?>> declaredSubstatements() {
        return substatements;
    }

    @Override
    public StatementSource getStatementSource() {
        return source;
    }

    @SuppressWarnings("unchecked")
    protected final <S extends DeclaredStatement<?>> Iterable<? extends S> allDeclared(Class<S> type) {
        return Iterable.class.cast(FluentIterable.from(substatements).filter(Predicates.instanceOf(type)));
    }
}
