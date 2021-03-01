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
import org.opendaylight.yangtools.odlext.model.api.ContextInstanceEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.ContextInstanceStatement;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class ContextInstanceStatementSupport
        extends AbstractStringStatementSupport<ContextInstanceStatement, ContextInstanceEffectiveStatement> {
    public static final @NonNull ContextInstanceStatementSupport INSTANCE = new ContextInstanceStatementSupport();

    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenDaylightExtensionsStatements.CONTEXT_INSTANCE).build();

    private ContextInstanceStatementSupport() {
        super(OpenDaylightExtensionsStatements.CONTEXT_INSTANCE, StatementPolicy.contextIndependent());
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return VALIDATOR;
    }

    @Override
    protected ContextInstanceStatement createDeclared(final StmtContext<String, ContextInstanceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ContextInstanceStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected ContextInstanceEffectiveStatement createEffective(final Current<String, ContextInstanceStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ContextInstanceEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
