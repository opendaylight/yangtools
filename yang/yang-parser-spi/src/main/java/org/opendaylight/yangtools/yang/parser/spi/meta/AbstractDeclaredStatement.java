/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

/**
 * Utility abstract base class for implementing declared statements.
 *
 * @param <A> Argument type.
 */
public abstract class AbstractDeclaredStatement<A> implements DeclaredStatement<A> {
    private final @NonNull ImmutableList<? extends DeclaredStatement<?>> substatements;
    private final @NonNull StatementDefinition definition;
    private final @NonNull StatementSource source;

    private final A argument;
    private final String rawArgument;

    protected AbstractDeclaredStatement(final StmtContext<A, ?, ?> context) {
        rawArgument = context.rawStatementArgument();
        argument = context.getStatementArgument();
        source = context.getStatementSource();
        definition = context.getPublicDefinition();
        /*
         * Perform an explicit copy, because Collections2.transform() is lazily transformed and retains pointer to
         * original collection, which may contains references to mutable context.
         */
        substatements = ImmutableList.copyOf(Collections2.transform(context.declaredSubstatements(),
            StmtContext::buildDeclared));
    }

    /**
     * Find first declared substatement of a particular type.
     *
     * @param type {@link DeclaredStatement} type
     * @return First effective substatement, or null if no match is found.
     * @deprecated Use {@link #findFirstDeclaredSubstatement(Class)} instead.
     */
    @Deprecated
    protected final <S extends DeclaredStatement<?>> S firstDeclared(final Class<S> type) {
        return findFirstDeclaredSubstatement(type).orElse(null);
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

    /**
     * Returns collection of explicitly declared child statements, while preserving its original ordering from original
     * source.
     *
     * @param type {@link DeclaredStatement} type
     * @return Collection of statements, which were explicitly declared in source of model.
     * @throws NullPointerException if {@code type} is null
     * @deprecated Use {@link #declaredSubstatements(Class)} instead.
     */
    @Deprecated
    protected final <S extends DeclaredStatement<?>> Collection<? extends S> allDeclared(final Class<S> type) {
        return declaredSubstatements(type);
    }
}
