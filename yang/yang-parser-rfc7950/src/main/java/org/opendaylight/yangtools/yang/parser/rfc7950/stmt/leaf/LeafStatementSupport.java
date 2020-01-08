/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class LeafStatementSupport
        extends AbstractQNameStatementSupport<LeafStatement, EffectiveStatement<QName, LeafStatement>> {
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
    public void onStatementAdded(final Mutable<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.coerceStatementArgument(), stmt);
    }

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
        super.onFullDefinitionDeclared(ctx);
        StmtContextUtils.validateIfFeatureAndWhenOnListKeys(ctx);
    }

    @Override
    public LeafStatement createDeclared(final StmtContext<QName, LeafStatement, ?> ctx) {
        return new LeafStatementImpl(ctx);
    }

    @Override
    public EffectiveStatement<QName, LeafStatement> createEffective(
            final StmtContext<QName, LeafStatement, EffectiveStatement<QName, LeafStatement>> ctx) {
        final LeafStatement declared = BaseStatementSupport.buildDeclared(ctx);
        final ImmutableList<? extends EffectiveStatement<?, ?>> substatements =
                AbstractEffectiveStatement.buildEffectiveSubstatements(ctx);


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
        final int flags = EffectiveStatementFlagMixin.createFlags(ctx.getCopyHistory(),
            findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT),
            ctx.isConfiguration(),
            findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE).booleanValue());
        final ImmutableList<MustDefinition> mustConstraints = substatements.stream()
                .filter(MustDefinition.class::isInstance)
                .map(MustDefinition.class::cast)
                .collect(ImmutableList.toImmutableList());

        return original != null || !mustConstraints.isEmpty()
                ?  new RegularLeafEffectiveStatement(declared, path, flags, substatements, mustConstraints, original)
                        : new EmptyLeafEffectiveStatement(declared, path, flags, substatements);
    }

    private static <E extends EffectiveStatement<?, ?>> @Nullable E findFirstStatement(
            final ImmutableList<? extends EffectiveStatement<?, ?>> statements, final Class<E> type) {
        for (EffectiveStatement<?, ?> stmt : statements) {
            if (type.isInstance(stmt)) {
                return type.cast(stmt);
            }
        }
        return null;
    }

    private static <A, E extends EffectiveStatement<A, ?>> A findFirstArgument(
            final ImmutableList<? extends EffectiveStatement<?, ?>> statements, final Class<E> type, final A defValue) {
        final @Nullable E stmt = findFirstStatement(statements, type);
        return stmt != null ? stmt.argument() : defValue;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}