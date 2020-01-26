/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
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
    private final String rawArgument;

    protected AbstractDeclaredStatement(final StmtContext<A, ?, ?> context) {
        rawArgument = context.rawStatementArgument();
    }

    @Override
    public final String rawArgument() {
        return rawArgument;
    }

    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

    public abstract static class RawArgument extends AbstractDeclaredStatement<String> {
        protected RawArgument(final StmtContext<String, ?, ?> context) {
            super(context);
        }

        @Override
        public final String argument() {
            return rawArgument();
        }
    }

    public abstract static class WithArgument<A> extends AbstractDeclaredStatement<A> {
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
