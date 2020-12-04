/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.rfc6643.model.api.ImpliedEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.ImpliedStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class ImpliedStatementSupport
        extends BaseStringStatementSupport<ImpliedStatement, ImpliedEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.IMPLIED).build();
    private static final ImpliedStatementSupport INSTANCE = new ImpliedStatementSupport();

    private ImpliedStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.IMPLIED, CopyPolicy.CONTEXT_INDEPENDENT);
    }

    public static ImpliedStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ImpliedStatement createDeclared(final StmtContext<String, ImpliedStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new ImpliedStatementImpl(ctx.getRawArgument(), substatements);
    }

    @Override
    protected ImpliedStatement createEmptyDeclared(final StmtContext<String, ImpliedStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected ImpliedEffectiveStatement createEffective(final Current<String, ImpliedStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new ImpliedEffectiveStatementImpl(stmt, substatements);
    }
}