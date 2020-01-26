/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * An abstract base class for {@link DeclaredStatement} implementations. This is a direct competition to
 * {@link org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement}, providing lower-footprint
 * implementations.
 */
@Beta
public abstract class AbstractDeclaredStatement<A> extends AbstractModelStatement<A> implements DeclaredStatement<A> {
    protected AbstractDeclaredStatement() {
    }

    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        // Default to reduce load on subclasses and keep the number of implementations down
        return ImmutableList.of();
    }

    /**
     * Utility method for recovering singleton lists squashed by {@link #maskList(ImmutableList)}.
     *
     * @param masked list to unmask
     * @return Unmasked list
     * @throws NullPointerException if masked is null
     * @throws ClassCastException if masked object does not match DeclaredStatement
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static final @NonNull ImmutableList<? extends DeclaredStatement<?>> unmaskList(
            final @NonNull Object masked) {
        return (ImmutableList) unmaskList(masked, DeclaredStatement.class);
    }

    public abstract static class WithRawArgument<A> extends AbstractDeclaredStatement<A> {
        private final String rawArgument;

        protected WithRawArgument(final StmtContext<A, ?, ?> context) {
            rawArgument = context.rawStatementArgument();
        }

        @Override
        public final String rawArgument() {
            return rawArgument;
        }
    }

    public abstract static class WithRawStringArgument extends WithRawArgument<String> {
        protected WithRawStringArgument(final StmtContext<String, ?, ?> context) {
            super(context);
        }

        @Override
        public final String argument() {
            return rawArgument();
        }
    }

    public abstract static class WithArgument<A> extends WithRawArgument<A> {
        private final A argument;

        protected WithArgument(final StmtContext<A, ?, ?> context) {
            super(context);
            argument = context.getStatementArgument();
        }

        @Override
        public final A argument() {
            return argument;
        }
    }
}
