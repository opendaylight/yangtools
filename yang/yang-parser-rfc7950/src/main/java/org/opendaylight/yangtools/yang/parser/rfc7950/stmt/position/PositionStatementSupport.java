/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.position;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PositionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseInternedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class PositionStatementSupport
        extends BaseInternedStatementSupport<Uint32, PositionStatement, PositionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.POSITION).build();
    private static final PositionStatementSupport INSTANCE = new PositionStatementSupport();

    private PositionStatementSupport() {
        super(YangStmtMapping.POSITION);
    }

    public static PositionStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public Uint32 parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return Uint32.valueOf(value).intern();
        } catch (IllegalArgumentException e) {
            throw new SourceException(String.format("Bit position value %s is not valid integer", value),
                    ctx.getStatementSourceReference(), e);
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected PositionStatement createDeclared(final Uint32 argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularPositionStatement(argument, substatements);
    }

    @Override
    protected PositionStatement createEmptyDeclared(final Uint32 argument) {
        return new EmptyPositionStatement(argument);
    }

    @Override
    protected PositionEffectiveStatement createEmptyEffective(final PositionStatement declared) {
        return new EmptyPositionEffectiveStatement(declared);
    }

    @Override
    protected PositionEffectiveStatement createEffective(
            final StmtContext<Uint32, PositionStatement, PositionEffectiveStatement> ctx,
            final PositionStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularPositionEffectiveStatement(declared, substatements);
    }
}