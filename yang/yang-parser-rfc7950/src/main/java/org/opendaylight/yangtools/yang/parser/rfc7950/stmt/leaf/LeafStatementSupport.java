/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class LeafStatementSupport extends BaseSchemaTreeStatementSupport<LeafStatement, LeafEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .LEAF)
        .addOptional(YangStmtMapping.CONFIG)
        .addOptional(YangStmtMapping.DEFAULT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.MANDATORY)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addMandatory(YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.UNITS)
        .addOptional(YangStmtMapping.WHEN)
        .build();
    private static final LeafStatementSupport INSTANCE = new LeafStatementSupport();

    private LeafStatementSupport() {
        super(YangStmtMapping.LEAF);
    }

    public static LeafStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx,value);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, LeafStatement, LeafEffectiveStatement> ctx) {
        super.onFullDefinitionDeclared(ctx);
        StmtContextUtils.validateIfFeatureAndWhenOnListKeys(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected LeafStatement createDeclared(final StmtContext<QName, LeafStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularLeafStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected LeafStatement createEmptyDeclared(final StmtContext<QName, LeafStatement, ?> ctx) {
        return new EmptyLeafStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected LeafEffectiveStatement createEffective(
            final StmtContext<QName, LeafStatement, LeafEffectiveStatement> ctx, final LeafStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
            findFirstStatement(substatements, TypeEffectiveStatement.class), ctx.getStatementSourceReference(),
                "Leaf is missing a 'type' statement");
        final String dflt = findFirstArgument(substatements, DefaultEffectiveStatement.class, null);
        SourceException.throwIf(
            EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(ctx.getRootVersion(), typeStmt, dflt),
            ctx.getStatementSourceReference(),
            "Leaf '%s' has default value '%s' marked with an if-feature statement.", ctx.getStatementArgument(), dflt);

        final SchemaPath path = ctx.getSchemaPath().get();
        final LeafSchemaNode original = (LeafSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective)
                .orElse(null);
        final int flags = new FlagsBuilder()
                .setHistory(ctx.getCopyHistory())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(ctx.isConfiguration())
                .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
                .toFlags();

        return original == null ? new EmptyLeafEffectiveStatement(declared, path, flags, substatements)
                : new RegularLeafEffectiveStatement(declared, path, flags, substatements, original);
    }

    @Override
    protected LeafEffectiveStatement createEmptyEffective(
            final StmtContext<QName, LeafStatement, LeafEffectiveStatement> ctx, final LeafStatement declared) {
        throw new UnsupportedOperationException("Leaf statements must have at least one substatement");
    }
}
