/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
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
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement.Ordering;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractLeafListStatementSupport
        extends BaseSchemaTreeStatementSupport<LeafListStatement, LeafListEffectiveStatement> {
    AbstractLeafListStatementSupport() {
        super(YangStmtMapping.LEAF_LIST);
    }

    @Override
    protected final LeafListStatement createDeclared(final StmtContext<QName, LeafListStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularLeafListStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected final LeafListStatement createEmptyDeclared(final StmtContext<QName, LeafListStatement, ?> ctx) {
        return new EmptyLeafListStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected final LeafListEffectiveStatement createEffective(
            final StmtContext<QName, LeafListStatement, LeafListEffectiveStatement> ctx,
            final LeafListStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
            findFirstStatement(substatements, TypeEffectiveStatement.class), ctx.getStatementSourceReference(),
                "Leaf-list is missing a 'type' statement");

        final SchemaPath path = ctx.getSchemaPath().get();
        final LeafListSchemaNode original = (LeafListSchemaNode) ctx.getOriginalCtx()
                .map(StmtContext::buildEffective).orElse(null);

        final int flags = new FlagsBuilder()
                .setHistory(ctx.getCopyHistory())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(ctx.isConfiguration())
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
            EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(ctx.getRootVersion(), typeStmt, defaultValues),
            ctx.getStatementSourceReference(),
            "Leaf-list '%s' has one of its default values '%s' marked with an if-feature statement.",
            ctx.getStatementArgument(), defaultValues);

        // FIXME: RFC7950 section 7.7.4: we need to check for min-elements and defaultValues conflict

        final Optional<ElementCountConstraint> elementCountConstraint =
                EffectiveStmtUtils.createElementCountConstraint(substatements);

        if (defaultValues.isEmpty()) {
            return original == null && !elementCountConstraint.isPresent()
                    ? new EmptyLeafListEffectiveStatement(declared, path, flags, substatements)
                            : new SlimLeafListEffectiveStatement(declared, path, flags, substatements, original,
                                elementCountConstraint.orElse(null));
        }

        return new RegularLeafListEffectiveStatement(declared, path, flags, substatements, original, defaultValues,
            elementCountConstraint.orElse(null));
    }

    @Override
    protected final LeafListEffectiveStatement createEmptyEffective(
            final StmtContext<QName, LeafListStatement, LeafListEffectiveStatement> ctx,
            final LeafListStatement declared) {
        throw new UnsupportedOperationException("Leaf statements must have at least one substatement");
    }
}