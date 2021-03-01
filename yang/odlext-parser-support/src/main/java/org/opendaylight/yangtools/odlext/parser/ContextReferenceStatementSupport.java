/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.ContextReferenceStatement;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class ContextReferenceStatementSupport
        extends AbstractStringStatementSupport<ContextReferenceStatement, ContextReferenceEffectiveStatement> {
    public static final @NonNull ContextReferenceStatementSupport INSTANCE = new ContextReferenceStatementSupport();

    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenDaylightExtensionsStatements.CONTEXT_REFERENCE).build();

    private ContextReferenceStatementSupport() {
        super(OpenDaylightExtensionsStatements.CONTEXT_REFERENCE, StatementPolicy.contextIndependent());
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return VALIDATOR;
    }

    @Override
    protected ContextReferenceStatement createDeclared(final StmtContext<String, ContextReferenceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ContextReferenceStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected ContextReferenceEffectiveStatement createEffective(final Current<String, ContextReferenceStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ContextReferenceEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
