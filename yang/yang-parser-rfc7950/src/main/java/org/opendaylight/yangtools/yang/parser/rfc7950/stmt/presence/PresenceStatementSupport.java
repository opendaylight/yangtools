/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.presence;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class PresenceStatementSupport
        extends BaseStringStatementSupport<PresenceStatement, PresenceEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(YangStmtMapping.PRESENCE).build();
    private static final PresenceStatementSupport INSTANCE = new PresenceStatementSupport();

    private PresenceStatementSupport() {
        super(YangStmtMapping.PRESENCE);
    }

    public static PresenceStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected PresenceStatement createDeclared(final StmtContext<String, PresenceStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularPresenceStatement(ctx, substatements);
    }

    @Override
    protected PresenceStatement createEmptyDeclared(final StmtContext<String, PresenceStatement, ?> ctx) {
        return new EmptyPresenceStatement(ctx.coerceRawStatementArgument());
    }

    @Override
    protected PresenceEffectiveStatement createEffective(
            final StmtContext<String, PresenceStatement, PresenceEffectiveStatement> ctx,
            final PresenceStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new RegularPresenceEffectiveStatement(declared, substatements);
    }

    @Override
    protected PresenceEffectiveStatement createEmptyEffective(
            final StmtContext<String, PresenceStatement, PresenceEffectiveStatement> ctx,
            final PresenceStatement declared) {
        return new EmptyPresenceEffectiveStatement(declared);
    }
}