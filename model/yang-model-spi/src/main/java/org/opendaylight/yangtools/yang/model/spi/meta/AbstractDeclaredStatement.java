/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * An abstract base class for {@link DeclaredStatement} implementations. It provides various further stateless and
 * stateful subclasses.
 */
@Beta
public abstract class AbstractDeclaredStatement extends AbstractModelStatement implements DeclaredStatement {
    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("rawArgument", rawArgument());
    }

    @Override
    public ImmutableList<? extends DeclaredStatement> declaredSubstatements() {
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
    protected static final @NonNull ImmutableList<? extends DeclaredStatement> unmaskList(
            final @NonNull Object masked) {
        return unmaskList(masked, DeclaredStatement.class);
    }

    public abstract static class WithArgument extends AbstractDeclaredStatement {
        public abstract static class WithSubstatements extends WithArgument {
            private final @NonNull Object substatements;

            protected WithSubstatements(final String rawArgument,
                    final ImmutableList<? extends DeclaredStatement> substatements) {
                super(rawArgument);
                this.substatements = maskList(substatements);
            }

            @Override
            public final ImmutableList<? extends DeclaredStatement> declaredSubstatements() {
                return unmaskList(substatements);
            }
        }

        private final String rawArgument;

        protected WithArgument(final String rawArgument) {
            this.rawArgument = rawArgument;
        }

        @Override
        public final String rawArgument() {
            return rawArgument;
        }
    }

    public abstract static class WithoutArgument extends AbstractDeclaredStatement {
        public abstract static class WithSubstatements extends WithoutArgument {
            private final @NonNull Object substatements;

            protected WithSubstatements(final ImmutableList<? extends DeclaredStatement> substatements) {
                this.substatements = maskList(substatements);
            }

            @Override
            public final ImmutableList<? extends DeclaredStatement> declaredSubstatements() {
                return unmaskList(substatements);
            }
        }

        @Override
        public final String rawArgument() {
            return null;
        }
    }
}
