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
import org.opendaylight.yangtools.rfc6643.model.api.DefValEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.DefValStatement;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class DefValStatementSupport
        extends BaseStringStatementSupport<DefValStatement, DefValEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.DEFVAL).build();
    private static final DefValStatementSupport INSTANCE = new DefValStatementSupport();

    private DefValStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.DEFVAL);
    }

    public static DefValStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected DefValStatement createDeclared(final StmtContext<String, DefValStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new DefValStatementImpl(ctx.coerceRawStatementArgument(), substatements);
    }

    @Override
    protected DefValStatement createEmptyDeclared(final StmtContext<String, DefValStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected DefValEffectiveStatement createEffective(
            final StmtContext<String, DefValStatement, DefValEffectiveStatement> ctx, final DefValStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new DefValEffectiveStatementImpl(declared, substatements, ctx);
    }

    @Override
    protected DefValEffectiveStatement createEmptyEffective(
            final StmtContext<String, DefValStatement, DefValEffectiveStatement> ctx, final DefValStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}