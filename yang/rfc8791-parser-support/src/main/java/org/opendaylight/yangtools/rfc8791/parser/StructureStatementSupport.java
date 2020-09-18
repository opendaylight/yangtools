/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureStatement;
import org.opendaylight.yangtools.rfc8791.model.api.YangDataStructureStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

final class StructureStatementSupport
        extends BaseQNameStatementSupport<StructureStatement, StructureEffectiveStatement> {
    private final SubstatementValidator validator;

    private StructureStatementSupport(final SubstatementValidator validator) {
        super(YangDataStructureStatements.STRUCTURE, CopyPolicy.REJECT);
        this.validator = requireNonNull(validator);
    }

    static StructureStatementSupport rfc6020() {
        return new StructureStatementSupport(
            SubstatementValidator.builder(YangDataStructureStatements.STRUCTURE)
                .addAny(YangStmtMapping.MUST)
                .addOptional(YangStmtMapping.STATUS)
                .addOptional(YangStmtMapping.DESCRIPTION)
                .addOptional(YangStmtMapping.REFERENCE)
                .addAny(YangStmtMapping.TYPEDEF)
                .addAny(YangStmtMapping.GROUPING)
                .addAny(YangStmtMapping.CONTAINER)
                .addAny(YangStmtMapping.LEAF)
                .addAny(YangStmtMapping.LEAF_LIST)
                .addAny(YangStmtMapping.LIST)
                .addAny(YangStmtMapping.CHOICE)
                .addAny(YangStmtMapping.ANYXML)
                .addAny(YangStmtMapping.USES)
                .build());
    }

    static StructureStatementSupport rfc7950() {
        return new StructureStatementSupport(
            SubstatementValidator.builder(YangDataStructureStatements.STRUCTURE)
                .addAny(YangStmtMapping.MUST)
                .addOptional(YangStmtMapping.STATUS)
                .addOptional(YangStmtMapping.DESCRIPTION)
                .addOptional(YangStmtMapping.REFERENCE)
                .addAny(YangStmtMapping.TYPEDEF)
                .addAny(YangStmtMapping.GROUPING)
                .addAny(YangStmtMapping.CONTAINER)
                .addAny(YangStmtMapping.LEAF)
                .addAny(YangStmtMapping.LEAF_LIST)
                .addAny(YangStmtMapping.LIST)
                .addAny(YangStmtMapping.CHOICE)
                .addAny(YangStmtMapping.ANYDATA)
                .addAny(YangStmtMapping.ANYXML)
                .addAny(YangStmtMapping.USES)
                .build());
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected StructureStatement createDeclared(final StmtContext<QName, StructureStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new StructureStatementImpl(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected StructureStatement createEmptyDeclared(final StmtContext<QName, StructureStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected StructureEffectiveStatement createEffective(
            final StmtContext<QName, StructureStatement, StructureEffectiveStatement> ctx,
            final StructureStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected StructureEffectiveStatement createEmptyEffective(
            final StmtContext<QName, StructureStatement, StructureEffectiveStatement> ctx,
            final StructureStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
