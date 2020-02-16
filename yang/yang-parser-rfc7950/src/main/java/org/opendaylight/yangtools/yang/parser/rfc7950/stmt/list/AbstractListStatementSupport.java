/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement.Ordering;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.ChildSchemaNodeNamespace;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractListStatementSupport extends BaseQNameStatementSupport<ListStatement, ListEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractListStatementSupport.class);

    AbstractListStatementSupport() {
        super(YangStmtMapping.LIST);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public final void onStatementAdded(final Mutable<QName, ListStatement, ListEffectiveStatement> stmt) {
        stmt.coerceParentContext().addToNs(ChildSchemaNodeNamespace.class, stmt.coerceStatementArgument(), stmt);
    }

    @Override
    protected final ListStatement createDeclared(final StmtContext<QName, ListStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularListStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected final ListStatement createEmptyDeclared(final StmtContext<QName, ListStatement, ?> ctx) {
        return new EmptyListStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected final ListEffectiveStatement createEffective(
            final StmtContext<QName, ListStatement, ListEffectiveStatement> ctx,
            final ListStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final StatementSourceReference ref = ctx.getStatementSourceReference();
        final SchemaPath path = ctx.getSchemaPath().get();
        final ListSchemaNode original = (ListSchemaNode) ctx.getOriginalCtx().map(StmtContext::buildEffective)
                .orElse(null);

        final ImmutableList<QName> keyDefinition;
        final KeyEffectiveStatement keyStmt = findFirstStatement(substatements, KeyEffectiveStatement.class);
        if (keyStmt != null) {
            final List<QName> keyDefinitionInit = new ArrayList<>(keyStmt.argument().size());
            final Set<QName> possibleLeafQNamesForKey = new HashSet<>();
            for (final EffectiveStatement<?, ?> effectiveStatement : substatements) {
                if (effectiveStatement instanceof LeafSchemaNode) {
                    possibleLeafQNamesForKey.add(((LeafSchemaNode) effectiveStatement).getQName());
                }
            }
            for (final SchemaNodeIdentifier key : keyStmt.argument()) {
                final QName keyQName = key.getNodeIdentifiers().get(0);

                if (!possibleLeafQNamesForKey.contains(keyQName)) {
                    throw new InferenceException(ref, "Key '%s' misses node '%s' in list '%s'",
                        keyStmt.getDeclared().rawArgument(), keyQName.getLocalName(), ctx.getStatementArgument());
                }
                keyDefinitionInit.add(keyQName);
            }

            keyDefinition = ImmutableList.copyOf(keyDefinitionInit);
        } else {
            keyDefinition = ImmutableList.of();
        }

        final boolean configuration = ctx.isConfiguration();
        final int flags = new FlagsBuilder()
                .setHistory(ctx.getCopyHistory())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(configuration)
                .setUserOrdered(findFirstArgument(substatements, OrderedByEffectiveStatement.class, Ordering.SYSTEM)
                    .equals(Ordering.USER))
                .toFlags();
        if (configuration && keyDefinition.isEmpty() && !inGrouping(ctx)) {
            LOG.info("Configuration list {} does not define any keys in violation of RFC7950 section 7.8.2. While "
                    + " this is fine with OpenDaylight, it can cause interoperability issues with other systems "
                    + "[at {}]", ctx.getStatementArgument(), ref);
        }

        final Optional<ElementCountConstraint> elementCountConstraint =
                EffectiveStmtUtils.createElementCountConstraint(substatements);
        return original == null && !elementCountConstraint.isPresent()
                ? new EmptyListEffectiveStatement(declared, path, flags, ctx, substatements, keyDefinition)
                        : new RegularListEffectiveStatement(declared, path, flags, ctx, substatements, keyDefinition,
                            elementCountConstraint.orElse(null), original);
    }

    private static boolean inGrouping(final StmtContext<?, ?, ?> ctx) {
        StmtContext<?, ?, ?> parent = ctx.getParentContext();
        while (parent != null) {
            if (parent.getPublicDefinition() == YangStmtMapping.GROUPING) {
                return true;
            }
            parent = parent.getParentContext();
        }
        return false;
    }

    @Override
    protected final ListEffectiveStatement createEmptyEffective(
            final StmtContext<QName, ListStatement, ListEffectiveStatement> ctx, final ListStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
