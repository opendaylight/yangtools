/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.enum_;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractEnumStatementSupport
        extends BaseStatementSupport<String, EnumStatement, EnumEffectiveStatement> {
    AbstractEnumStatementSupport() {
        super(YangStmtMapping.ENUM);
    }

    @Override
    public final String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // FIXME: Checks for real value
        return value;
    }

    @Override
    protected final EnumStatement createDeclared(final StmtContext<String, EnumStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularEnumStatement(ctx, substatements);
    }

    @Override
    protected final EnumStatement createEmptyDeclared(final StmtContext<String, EnumStatement, ?> ctx) {
        return new EmptyEnumStatement(ctx);
    }

    @Override
    protected final EnumEffectiveStatement createEffective(
            final StmtContext<String, EnumStatement, EnumEffectiveStatement> ctx, final EnumStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularEnumEffectiveStatement(declared, substatements, new FlagsBuilder()
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .toFlags());
    }

    @Override
    protected final EnumEffectiveStatement createEmptyEffective(
            final StmtContext<String, EnumStatement, EnumEffectiveStatement> ctx, final EnumStatement declared) {
        return new EmptyEnumEffectiveStatement(declared);
    }
}