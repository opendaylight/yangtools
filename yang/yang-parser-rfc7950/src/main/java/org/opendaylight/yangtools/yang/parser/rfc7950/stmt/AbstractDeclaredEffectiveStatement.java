/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class AbstractDeclaredEffectiveStatement<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveStatement<A, D> {
    @Override
    public final StatementSource getStatementSource() {
        return StatementSource.DECLARATION;
    }

    @Override
    public final StatementDefinition statementDefinition() {
        return getDeclared().statementDefinition();
    }

    @Override
    public abstract @NonNull D getDeclared();

    public static <A, D extends DeclaredStatement<A>> @NonNull D buildDeclared(final StmtContext<A, D, ?> ctx) {
        /*
         * Share original instance of declared statement between all effective
         * statements which have been copied or derived from this original
         * declared statement.
         */
        @SuppressWarnings("unchecked")
        final StmtContext<?, D, ?> lookupCtx = (StmtContext<?, D, ?>) ctx.getOriginalCtx().orElse(ctx);
        return verifyNotNull(lookupCtx.buildDeclared(), "Statement %s failed to build declared statement", lookupCtx);
    }

    public abstract static class Default<A, D extends DeclaredStatement<A>>
            extends AbstractDeclaredEffectiveStatement<A, D> {
        private final @NonNull D declared;

        protected Default(final D declared) {
            this.declared = requireNonNull(declared);
        }

        @Override
        public final D getDeclared() {
            return declared;
        }
    }
}
