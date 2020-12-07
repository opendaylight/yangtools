/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
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
        super(YangStmtMapping.LEAF, CopyPolicy.DECLARED_COPY);
    }

    public static LeafStatementSupport getInstance() {
        return INSTANCE;
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
        return new RegularLeafStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected LeafStatement createEmptyDeclared(final StmtContext<QName, LeafStatement, ?> ctx) {
        return new EmptyLeafStatement(ctx.getArgument());
    }

    @Override
    protected LeafEffectiveStatement createEffective(final Current<QName, LeafStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
            findFirstStatement(substatements, TypeEffectiveStatement.class), stmt,
            "Leaf is missing a 'type' statement");
        final String dflt = findFirstArgument(substatements, DefaultEffectiveStatement.class, null);
        SourceException.throwIf(
            EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(stmt.yangVersion(), typeStmt, dflt), stmt,
            "Leaf '%s' has default value '%s' marked with an if-feature statement.", stmt.argument(), dflt);

        final LeafSchemaNode original = (LeafSchemaNode) stmt.original();
        final int flags = new FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(stmt.effectiveConfig().asNullable())
                .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
                .toFlags();

        final LeafStatement declared = stmt.declared();
        final SchemaPath path = stmt.wrapSchemaPath();
        return original == null ? new EmptyLeafEffectiveStatement(declared, path, flags, substatements)
                : new RegularLeafEffectiveStatement(declared, path, flags, substatements, original);
    }

    @Override
    public boolean copyEffective(@NonNull LeafEffectiveStatement original, Current<QName, LeafStatement> stmt) {
        if (!((AbstractLeafEffectiveStatement) original).getPath().equals(stmt.wrapSchemaPath())) {
            return false;
        }
        if (((AbstractLeafEffectiveStatement) original).isAddedByUses()
                != stmt.history().contains(CopyType.ADDED_BY_USES)) {
            return false;
        }
        if (((AbstractLeafEffectiveStatement) original).isAugmenting()
                != stmt.history().contains(CopyType.ADDED_BY_AUGMENTATION)) {
            return false;
        }
        if (((AbstractLeafEffectiveStatement) original).effectiveConfig()
                .equals(Optional.ofNullable(stmt.effectiveConfig().asNullable()))) {
            return false;
        }
        return true;
    }
}
