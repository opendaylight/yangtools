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
import org.opendaylight.yangtools.rfc6643.model.api.AliasEffectiveStatement;
import org.opendaylight.yangtools.rfc6643.model.api.AliasStatement;
import org.opendaylight.yangtools.rfc6643.model.api.IetfYangSmiv2ExtensionsMapping;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStringStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

@Beta
public final class AliasStatementSupport
        extends BaseStringStatementSupport<AliasStatement, AliasEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
            SubstatementValidator.builder(IetfYangSmiv2ExtensionsMapping.ALIAS)
                .add(YangStmtMapping.DESCRIPTION, 0, 1)
                .add(YangStmtMapping.REFERENCE, 0, 1)
                .add(YangStmtMapping.STATUS, 0, 1)
                .add(IetfYangSmiv2ExtensionsMapping.OBJECT_ID, 0, 1)
                .build();
    private static final AliasStatementSupport INSTANCE = new AliasStatementSupport();

    private AliasStatementSupport() {
        super(IetfYangSmiv2ExtensionsMapping.ALIAS);
    }

    public static AliasStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected AliasStatement createDeclared(final StmtContext<String, AliasStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new AliasStatementImpl(ctx.coerceRawStatementArgument(), substatements);
    }

    @Override
    protected AliasStatement createEmptyDeclared(final StmtContext<String, AliasStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected AliasEffectiveStatement createEffective(
            final StmtContext<String, AliasStatement, AliasEffectiveStatement> ctx, final AliasStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new AliasEffectiveStatementImpl(ctx, substatements);
    }

    @Override
    protected AliasEffectiveStatement createEmptyEffective(
            final StmtContext<String, AliasStatement, AliasEffectiveStatement> ctx, final AliasStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
