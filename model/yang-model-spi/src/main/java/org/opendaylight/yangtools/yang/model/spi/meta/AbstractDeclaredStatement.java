/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementOrigin;

/**
 * An abstract base class for {@link DeclaredStatement} implementations. It provides various further stateless and
 * stateful subclasses.
 */
@Beta
public abstract class AbstractDeclaredStatement<A> extends AbstractModelStatement<A> implements DeclaredStatement<A> {
    @Override
    public StatementOrigin statementOrigin() {
        return StatementOrigin.DECLARATION;
    }

    @Override
    public ImmutableList<? extends DeclaredStatement<?>> declaredSubstatements() {
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
        public abstract static class WithSubstatements<A> extends WithRawArgument<A> {
            private final @NonNull Object substatements;

            protected WithSubstatements(final String rawArgument,
                    final ImmutableList<? extends DeclaredStatement<?>> substatements) {
                super(rawArgument);
                this.substatements = maskList(substatements);
            }

            @Override
            public final ImmutableList<? extends DeclaredStatement<?>> declaredSubstatements() {
                return unmaskList(substatements);
            }
        }

        private final String rawArgument;

        protected WithRawArgument(final String rawArgument) {
            this.rawArgument = rawArgument;
        }

        @Override
        public final String rawArgument() {
            return rawArgument;
        }
    }

    public abstract static class WithQNameArgument extends AbstractDeclaredStatement<QName> {
        public abstract static class WithSubstatements extends WithQNameArgument {
            private final @NonNull Object substatements;

            protected WithSubstatements(final QName argument,
                    final ImmutableList<? extends DeclaredStatement<?>> substatements) {
                super(argument);
                this.substatements = maskList(substatements);
            }

            @Override
            public final ImmutableList<? extends DeclaredStatement<?>> declaredSubstatements() {
                return unmaskList(substatements);
            }
        }

        private final @NonNull QName argument;

        protected WithQNameArgument(final QName argument) {
            this.argument = requireNonNull(argument);
        }

        @Override
        public final QName argument() {
            return argument;
        }

        @Override
        public final String rawArgument() {
            return argument.getLocalName();
        }
    }

    public abstract static class WithRawStringArgument extends WithRawArgument<String> {
        public abstract static class WithSubstatements extends WithRawStringArgument {
            private final @NonNull Object substatements;

            protected WithSubstatements(final String rawArgument,
                    final ImmutableList<? extends DeclaredStatement<?>> substatements) {
                super(rawArgument);
                this.substatements = maskList(substatements);
            }

            @Override
            public final ImmutableList<? extends DeclaredStatement<?>> declaredSubstatements() {
                return unmaskList(substatements);
            }
        }

        protected WithRawStringArgument(final String rawArgument) {
            super(verifyNotNull(rawArgument));
        }

        @Override
        public final String argument() {
            return verifyNotNull(rawArgument());
        }
    }

    public abstract static class WithArgument<A> extends WithRawArgument<A> {
        public abstract static class WithSubstatements<A> extends WithArgument<A> {
            private final @NonNull Object substatements;

            protected WithSubstatements(final String rawArgument, final A argument,
                    final ImmutableList<? extends DeclaredStatement<?>> substatements) {
                super(rawArgument, argument);
                this.substatements = maskList(substatements);
            }

            @Override
            public final ImmutableList<? extends DeclaredStatement<?>> declaredSubstatements() {
                return unmaskList(substatements);
            }
        }

        private final @NonNull A argument;

        protected WithArgument(final String rawArgument, final A argument) {
            super(rawArgument);
            this.argument = requireNonNull(argument);
        }

        @Override
        public final A argument() {
            return argument;
        }
    }

    public abstract static class ArgumentToString<A> extends AbstractDeclaredStatement<A> {
        public abstract static class WithSubstatements<A> extends ArgumentToString<A> {
            private final @NonNull Object substatements;

            protected WithSubstatements(final A argument,
                    final ImmutableList<? extends DeclaredStatement<?>> substatements) {
                super(argument);
                this.substatements = maskList(substatements);
            }

            @Override
            public final ImmutableList<? extends DeclaredStatement<?>> declaredSubstatements() {
                return unmaskList(substatements);
            }
        }

        private final @NonNull A argument;

        protected ArgumentToString(final @NonNull A argument) {
            this.argument = requireNonNull(argument);
        }

        @Override
        public final A argument() {
            return argument;
        }

        @Override
        public final String rawArgument() {
            return argument.toString();
        }
    }

    public abstract static class WithoutArgument extends AbstractDeclaredStatement<Empty> {
        public abstract static class WithSubstatements extends WithoutArgument {
            private final @NonNull Object substatements;

            protected WithSubstatements(final ImmutableList<? extends DeclaredStatement<?>> substatements) {
                this.substatements = maskList(substatements);
            }

            @Override
            public final ImmutableList<? extends DeclaredStatement<?>> declaredSubstatements() {
                return unmaskList(substatements);
            }
        }

        @Override
        public final Empty argument() {
            return Empty.value();
        }

        @Override
        public final String rawArgument() {
            return null;
        }
    }
}
