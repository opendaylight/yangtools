/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameWithFlagsEffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class LeafStatementSupport
        extends AbstractSchemaTreeStatementSupport<LeafStatement, LeafEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.LEAF)
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

    public LeafStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.LEAF, instantiatedPolicy(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, LeafStatement, LeafEffectiveStatement> ctx) {
        super.onFullDefinitionDeclared(ctx);
        StmtContextUtils.validateIfFeatureAndWhenOnListKeys(ctx);
    }

    @Override
    protected LeafStatement createDeclared(final StmtContext<QName, LeafStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createLeaf(ctx.getArgument(), substatements);
    }

    @Override
    protected LeafStatement attachDeclarationReference(final LeafStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateLeaf(stmt, reference);
    }

    @Override
    public LeafEffectiveStatement copyEffective(final Current<QName, LeafStatement> stmt,
            final LeafEffectiveStatement original) {
        return EffectiveStatements.copyLeaf(original, stmt.getArgument(),
            computeFlags(stmt, original.effectiveSubstatements()), stmt.original(LeafSchemaNode.class));
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

        return EffectiveStatements.createLeaf(stmt.declared(), stmt.getArgument(), computeFlags(stmt, substatements),
            substatements, stmt.original(LeafSchemaNode.class));
    }

    @Override
    public EffectiveStatementState extractEffectiveState(final LeafEffectiveStatement stmt) {
        verify(stmt instanceof LeafSchemaNode, "Unexpected statement %s", stmt);
        final var schema = (LeafSchemaNode) stmt;
        return new QNameWithFlagsEffectiveStatementState(stmt.argument(), new FlagsBuilder()
            .setHistory(schema)
            .setStatus(schema.getStatus())
            .setConfiguration(schema.effectiveConfig().orElse(null))
            .setMandatory(schema.isMandatory())
            .toFlags());
    }

    private static int computeFlags(final Current<?, ?> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
            .setHistory(stmt.history())
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .setConfiguration(stmt.effectiveConfig().asNullable())
            .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
            .toFlags();
    }
}
