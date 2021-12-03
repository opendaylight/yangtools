/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class LeafListStatementSupport
        extends AbstractSchemaTreeStatementSupport<LeafListStatement, LeafListEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.LEAF_LIST)
            .addOptional(YangStmtMapping.CONFIG)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addOptional(YangStmtMapping.MIN_ELEMENTS)
            .addOptional(YangStmtMapping.MAX_ELEMENTS)
            .addAny(YangStmtMapping.MUST)
            .addOptional(YangStmtMapping.ORDERED_BY)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addMandatory(YangStmtMapping.TYPE)
            .addOptional(YangStmtMapping.UNITS)
            .addOptional(YangStmtMapping.WHEN)
            .build();
    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.LEAF_LIST)
            .addOptional(YangStmtMapping.CONFIG)
            .addAny(YangStmtMapping.DEFAULT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addOptional(YangStmtMapping.MIN_ELEMENTS)
            .addOptional(YangStmtMapping.MAX_ELEMENTS)
            .addAny(YangStmtMapping.MUST)
            .addOptional(YangStmtMapping.ORDERED_BY)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addMandatory(YangStmtMapping.TYPE)
            .addOptional(YangStmtMapping.UNITS)
            .addOptional(YangStmtMapping.WHEN)
            .build();

    private LeafListStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.LEAF_LIST, instantiatedPolicy(), config, validator);
    }

    public static @NonNull LeafListStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new LeafListStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull LeafListStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new LeafListStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    protected LeafListStatement createDeclared(final StmtContext<QName, LeafListStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createLeafList(ctx.getArgument(), substatements);
    }

    @Override
    protected LeafListStatement attachDeclarationReference(final LeafListStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateLeafList(stmt, reference);
    }

    @Override
    public LeafListEffectiveStatement copyEffective(final Current<QName, LeafListStatement> stmt,
            final LeafListEffectiveStatement original) {
        return EffectiveStatements.copyLeafList(original, stmt.getArgument(),
            computeFlags(stmt, original.effectiveSubstatements()), stmt.original(LeafListSchemaNode.class));
    }

    @Override
    protected LeafListEffectiveStatement createEffective(final Current<QName, LeafListStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
                findFirstStatement(substatements, TypeEffectiveStatement.class), stmt,
                "Leaf-list is missing a 'type' statement");

        final ImmutableSet<String> defaultValues = substatements.stream()
                .filter(DefaultEffectiveStatement.class::isInstance)
                .map(DefaultEffectiveStatement.class::cast)
                .map(DefaultEffectiveStatement::argument)
                .collect(ImmutableSet.toImmutableSet());

        // FIXME: We need to interpret the default value in terms of supplied element type
        SourceException.throwIf(
                EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(stmt.yangVersion(), typeStmt, defaultValues),
                stmt, "Leaf-list '%s' has one of its default values '%s' marked with an if-feature statement.",
                stmt.argument(), defaultValues);

        // FIXME: RFC7950 section 7.7.4: we need to check for min-elements and defaultValues conflict

        return EffectiveStatements.createLeafList(stmt.declared(), stmt.getArgument(),
            computeFlags(stmt, substatements), substatements, defaultValues,
            EffectiveStmtUtils.createElementCountConstraint(substatements).orElse(null),
            stmt.original(LeafListSchemaNode.class));
    }

    @Override
    public EffectiveStatementState extractEffectiveState(final LeafListEffectiveStatement stmt) {
        verify(stmt instanceof LeafListSchemaNode, "Unexpected statement %s", stmt);
        final var schema = (LeafListSchemaNode) stmt;
        return new QNameWithFlagsEffectiveStatementState(stmt.argument(), new FlagsBuilder()
            .setHistory(schema)
            .setStatus(schema.getStatus())
            .setConfiguration(schema.effectiveConfig().orElse(null))
            .setUserOrdered(schema.isUserOrdered())
            .toFlags());
    }

    private static int computeFlags(final Current<?, ?> stmt,
        final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
            .setHistory(stmt.history())
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .setConfiguration(stmt.effectiveConfig().asNullable())
            .setUserOrdered(findFirstArgument(substatements, OrderedByEffectiveStatement.class, Ordering.SYSTEM)
                .equals(Ordering.USER))
            .toFlags();
    }
}
