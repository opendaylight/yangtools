/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent.EffectiveConfig;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractListStatementSupport extends
        BaseSchemaTreeStatementSupport<ListStatement, ListEffectiveStatement> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractListStatementSupport.class);
    private static final ImmutableSet<YangStmtMapping> UNINSTANTIATED_DATATREE_STATEMENTS = ImmutableSet.of(
        YangStmtMapping.GROUPING, YangStmtMapping.NOTIFICATION, YangStmtMapping.INPUT, YangStmtMapping.OUTPUT);

    AbstractListStatementSupport() {
        super(YangStmtMapping.LIST);
    }

    @Override
    protected final ListStatement createDeclared(final StmtContext<QName, ListStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularListStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected final ListStatement createEmptyDeclared(final StmtContext<QName, ListStatement, ?> ctx) {
        return new EmptyListStatement(ctx.getArgument());
    }

    @Override
    protected ListEffectiveStatement createEffective(final Current<QName, ListStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final ListSchemaNode original = (ListSchemaNode) stmt.original();

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
            for (final QName keyQName : keyStmt.argument()) {
                if (!possibleLeafQNamesForKey.contains(keyQName)) {
                    throw new InferenceException(stmt, "Key '%s' misses node '%s' in list '%s'",
                        keyStmt.getDeclared().rawArgument(), keyQName.getLocalName(), stmt.argument());
                }
                keyDefinitionInit.add(keyQName);
            }

            keyDefinition = ImmutableList.copyOf(keyDefinitionInit);
        } else {
            keyDefinition = ImmutableList.of();
        }

        final EffectiveConfig configuration = stmt.effectiveConfig();
        final int flags = new FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(configuration.asNullable())
                .setUserOrdered(findFirstArgument(substatements, OrderedByEffectiveStatement.class, Ordering.SYSTEM)
                    .equals(Ordering.USER))
                .toFlags();
        if (configuration == EffectiveConfig.TRUE && keyDefinition.isEmpty() && isInstantied(stmt)) {
            warnConfigList(stmt);
        }

        EffectiveStmtUtils.checkUniqueGroupings(stmt, substatements);
        EffectiveStmtUtils.checkUniqueTypedefs(stmt, substatements);
        EffectiveStmtUtils.checkUniqueUses(stmt, substatements);

        final Optional<ElementCountConstraint> elementCountConstraint =
            EffectiveStmtUtils.createElementCountConstraint(substatements);
        final SchemaPath path = stmt.wrapSchemaPath();
        try {
            return original == null && !elementCountConstraint.isPresent()
                ? new EmptyListEffectiveStatement(stmt.declared(), path, flags, substatements, keyDefinition)
                    : new RegularListEffectiveStatement(stmt.declared(), path, flags, substatements, keyDefinition,
                        elementCountConstraint.orElse(null), original);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    private static void warnConfigList(final @NonNull Current<QName, ListStatement> stmt) {
        final StatementSourceReference ref = stmt.sourceReference();
        final Boolean warned = stmt.getFromNamespace(ConfigListWarningNamespace.class, ref);
        // Hacky check if we have issued a warning for the original statement
        if (warned == null) {
            final StmtContext<?, ?, ?> ctx = stmt.caerbannog();
            verify(ctx instanceof Mutable, "Unexpected context %s", ctx);
            ((Mutable<?, ?, ?>) ctx).addToNs(ConfigListWarningNamespace.class, ref, Boolean.TRUE);
            LOG.info("Configuration list {} does not define any keys in violation of RFC7950 section 7.8.2. While "
                    + "this is fine with OpenDaylight, it can cause interoperability issues with other systems "
                    + "[defined at {}]", stmt.argument(), ref);
        }
    }

    private static boolean isInstantied(final EffectiveStmtCtx ctx) {
        Parent parent = ctx.effectiveParent();
        while (parent != null) {
            final StatementDefinition parentDef = parent.publicDefinition();
            if (UNINSTANTIATED_DATATREE_STATEMENTS.contains(parentDef)) {
                return false;
            }

            final Parent grandParent = parent.effectiveParent();
            if (YangStmtMapping.AUGMENT == parentDef && grandParent != null) {
                // If this is an augment statement and its parent is either a 'module' or 'submodule' statement, we are
                // dealing with an uninstantiated context.
                final StatementDefinition grandParentDef = grandParent.publicDefinition();
                if (YangStmtMapping.MODULE == grandParentDef || YangStmtMapping.SUBMODULE == grandParentDef) {
                    return false;
                }
            }

            parent = grandParent;
        }
        return true;
    }
}
