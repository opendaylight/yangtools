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
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccess;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class MaxAccessStatementSupport
        extends BaseStatementSupport<MaxAccess, MaxAccessStatement, MaxAccessEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.MAX_ACCESS).build();
    private static final MaxAccessStatementSupport INSTANCE = new MaxAccessStatementSupport();

    private MaxAccessStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.MAX_ACCESS);
    }

    public static MaxAccessStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public MaxAccess parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return MaxAccess.forStringLiteral(value);
        } catch (IllegalArgumentException e) {
            throw new SourceException(ctx.getStatementSourceReference(), e, "Invalid max-access value '%s'", value);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected MaxAccessStatement createDeclared(final StmtContext<MaxAccess, MaxAccessStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new MaxAccessStatementImpl(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected MaxAccessStatement createEmptyDeclared(final StmtContext<MaxAccess, MaxAccessStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected MaxAccessEffectiveStatement createEffective(
            final StmtContext<MaxAccess, MaxAccessStatement, MaxAccessEffectiveStatement> ctx,
            final MaxAccessStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new MaxAccessEffectiveStatementImpl(ctx, substatements);
    }

    @Override
    protected MaxAccessEffectiveStatement createEmptyEffective(
            final StmtContext<MaxAccess, MaxAccessStatement, MaxAccessEffectiveStatement> ctx,
            final MaxAccessStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}