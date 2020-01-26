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
public abstract class AbstractDeclaredStatement<A> implements DeclaredStatement<A> {
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
     * Utility method for squashing singleton lists into single objects. This is a CPU/mem trade-off, which we are
     * usually willing to make: for the cost of an instanceof check we can save one object and re-create it when needed.
     * The inverse operation is #unmaskSubstatements(Object)}.
     *
     * @param substatements substatements to mask
     * @return Masked substatements
     * @throws NullPointerException if substatements is null
     */
    protected static final @NonNull Object maskSubstatements(
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        // Note: ImmutableList guarantees non-null content
        return substatements.size() == 1 ? substatements.get(0) : substatements;
    }

    /**
     * Utility method for recovering singleton lists squashed by {@link #maskSubstatements(ImmutableList)}.
     *
     * @param masked substatements to unmask
     * @return List of substatements
     * @throws NullPointerException if masked is null
     * @throws ClassCastException if masked object was not produced by {@link #maskSubstatements(ImmutableList)}
     */
    @SuppressWarnings("unchecked")
    protected static final @NonNull ImmutableList<? extends DeclaredStatement<?>> unmaskSubstatements(
            final @NonNull Object masked) {
        return masked instanceof ImmutableList ? (ImmutableList<? extends DeclaredStatement<?>>) masked
                // Yes, this is ugly code, which could use an explicit verify, that would just change the what sort
                // of exception we throw. ClassCastException is as good as VerifyException.
                : ImmutableList.of((DeclaredStatement<?>) masked);
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
