/*
 * Copyright (c) 2015, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

/**
 * Utility abstract base class for implementing declared statements.
 *
 *
 * @param <A> Argument type.
 */
public abstract class AbstractDeclaredStatement<A> implements DeclaredStatement<A> {


    private final A argument;
    private final String rawArgument;
    private final ImmutableList<? extends DeclaredStatement<?>> substatements;
    private final StatementDefinition definition;
    private final StatementSource source;

    protected AbstractDeclaredStatement(final StmtContext<A,?,?> context) {
        rawArgument = context.rawStatementArgument();
        argument = context.getStatementArgument();
        source = context.getStatementSource();
        definition = context.getPublicDefinition();
        /*
         * Perform an explicit copy to avoid keeping references to the original collection, which may contain
         * references to mutable context.
         */
        substatements = ImmutableList.copyOf(
                context.declaredSubstatements().stream().map(StmtContextUtils.buildDeclared()).collect(
                        Collectors.toList()));
    }

    protected final <S extends DeclaredStatement<?>> S firstDeclared(final Class<S> type) {
        return type.cast(substatements.stream().filter(Predicates.instanceOf(type)::apply).findFirst().orElse(null));
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
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return substatements;
    }

    @Override
    public StatementSource getStatementSource() {
        return source;
    }

    @SuppressWarnings("unchecked")
    protected final <S extends DeclaredStatement<?>> Collection<? extends S> allDeclared(final Class<S> type) {
        return Collection.class.cast(Collections2.filter(substatements, Predicates.instanceOf(type)));
    }
}
