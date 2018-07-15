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
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;

/**
 * Utility abstract base class for implementing declared statements.
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

    @Nonnull
    @Override
    public StatementDefinition statementDefinition() {
        return definition;
    }

    @Nonnull
    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return substatements;
    }

    @Nonnull
    @Override
    public StatementSource getStatementSource() {
        return source;
    }

    protected final <S extends DeclaredStatement<?>> Collection<? extends S> allDeclared(final Class<S> type) {
        return Collections2.transform(Collections2.filter(substatements, type::isInstance), type::cast);
    }
}
