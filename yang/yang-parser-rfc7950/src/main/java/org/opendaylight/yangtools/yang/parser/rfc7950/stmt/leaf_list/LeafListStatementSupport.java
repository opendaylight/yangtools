/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class LeafListStatementSupport
        extends BaseSchemaTreeStatementSupport<LeafListStatement, LeafListEffectiveStatement> {
    private static final @NonNull LeafListStatementSupport RFC6020_INSTANCE = new LeafListStatementSupport(
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
            .build());
    private static final @NonNull LeafListStatementSupport RFC7950_INSTANCE = new LeafListStatementSupport(
        SubstatementValidator.builder(YangStmtMapping
            .LEAF_LIST)
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
            .build());

    private final SubstatementValidator validator;

    private LeafListStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.LEAF_LIST, StatementPolicy.legacyDeclaredCopy());
        this.validator = requireNonNull(validator);
    }

    public static @NonNull LeafListStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull LeafListStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected LeafListStatement createDeclared(final StmtContext<QName, LeafListStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularLeafListStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected LeafListStatement createEmptyDeclared(final StmtContext<QName, LeafListStatement, ?> ctx) {
        return new EmptyLeafListStatement(ctx.getArgument());
    }

    @Override
    protected LeafListEffectiveStatement createEffective(final Current<QName, LeafListStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
                findFirstStatement(substatements, TypeEffectiveStatement.class), stmt,
                "Leaf-list is missing a 'type' statement");

        final LeafListSchemaNode original = (LeafListSchemaNode) stmt.original();

        final int flags = new FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(stmt.effectiveConfig().asNullable())
                .setUserOrdered(findFirstArgument(substatements, OrderedByEffectiveStatement.class, Ordering.SYSTEM)
                    .equals(Ordering.USER))
                .toFlags();
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

        final Optional<ElementCountConstraint> elementCountConstraint =
                EffectiveStmtUtils.createElementCountConstraint(substatements);

        final LeafListStatement declared = stmt.declared();
        final SchemaPath path = stmt.wrapSchemaPath();
        if (defaultValues.isEmpty()) {
            return original == null && !elementCountConstraint.isPresent()
                ? new EmptyLeafListEffectiveStatement(declared, path, flags, substatements)
                    : new SlimLeafListEffectiveStatement(declared, path, flags, substatements, original,
                        elementCountConstraint.orElse(null));
        }

        return new RegularLeafListEffectiveStatement(declared, path, flags, substatements, original, defaultValues,
            elementCountConstraint.orElse(null));
    }
}