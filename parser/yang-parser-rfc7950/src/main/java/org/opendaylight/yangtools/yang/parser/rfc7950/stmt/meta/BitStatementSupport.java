/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BitStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class BitStatementSupport extends AbstractStatementSupport<String, BitStatement, BitEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.BIT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.POSITION)
        .build();
    private static final SubstatementValidator RFC7950_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.BIT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.POSITION)
        .build();

    private BitStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.BIT, StatementPolicy.contextIndependent(), config, requireNonNull(validator));
    }

    public static @NonNull BitStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new BitStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull BitStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new BitStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // Performs de-duplication and interning in one go
        return ctx.parseIdentifier(value).getLocalName();
    }

    @Override
    protected BitStatement createDeclared(final BoundStmtCtx<String> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createBit(ctx.getArgument(), substatements);
    }

    @Override
    protected BitStatement attachDeclarationReference(final BitStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateBit(stmt, reference);
    }

    @Override
    protected BitEffectiveStatement createEffective(final Current<String, BitStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createBit(stmt.declared(), substatements);
    }
}
