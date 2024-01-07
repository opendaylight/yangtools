/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.list;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Ordering;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Parent.EffectiveConfig;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameWithFlagsEffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public final class ListStatementSupport
        extends AbstractSchemaTreeStatementSupport<ListStatement, ListEffectiveStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(ListStatementSupport.class);
    private static final ImmutableSet<YangStmtMapping> UNINSTANTIATED_DATATREE_STATEMENTS = ImmutableSet.of(
        YangStmtMapping.GROUPING, YangStmtMapping.NOTIFICATION, YangStmtMapping.INPUT, YangStmtMapping.OUTPUT);

    private static final SubstatementValidator RFC6020_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.LIST)
        .addAny(YangStmtMapping.ANYXML)
        .addAny(YangStmtMapping.CHOICE)
        .addOptional(YangStmtMapping.CONFIG)
        .addAny(YangStmtMapping.CONTAINER)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.KEY)
        .addAny(YangStmtMapping.LEAF)
        .addAny(YangStmtMapping.LEAF_LIST)
        .addAny(YangStmtMapping.LIST)
        .addOptional(YangStmtMapping.MAX_ELEMENTS)
        .addOptional(YangStmtMapping.MIN_ELEMENTS)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.ORDERED_BY)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addAny(YangStmtMapping.TYPEDEF)
        .addAny(YangStmtMapping.UNIQUE)
        .addAny(YangStmtMapping.USES)
        .addOptional(YangStmtMapping.WHEN)
        .build();
    private static final SubstatementValidator RFC7950_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.LIST)
        .addAny(YangStmtMapping.ACTION)
        .addAny(YangStmtMapping.ANYDATA)
        .addAny(YangStmtMapping.ANYXML)
        .addAny(YangStmtMapping.CHOICE)
        .addOptional(YangStmtMapping.CONFIG)
        .addAny(YangStmtMapping.CONTAINER)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.KEY)
        .addAny(YangStmtMapping.LEAF)
        .addAny(YangStmtMapping.LEAF_LIST)
        .addAny(YangStmtMapping.LIST)
        .addOptional(YangStmtMapping.MAX_ELEMENTS)
        .addOptional(YangStmtMapping.MIN_ELEMENTS)
        .addAny(YangStmtMapping.MUST)
        .addAny(YangStmtMapping.NOTIFICATION)
        .addOptional(YangStmtMapping.ORDERED_BY)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addAny(YangStmtMapping.TYPEDEF)
        .addAny(YangStmtMapping.UNIQUE)
        .addAny(YangStmtMapping.USES)
        .addOptional(YangStmtMapping.WHEN)
        .build();

    private final boolean warnForUnkeyedLists;

    ListStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.LIST, instantiatedPolicy(), config, validator);
        warnForUnkeyedLists = config.warnForUnkeyedLists();
    }

    public static @NonNull ListStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new ListStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull ListStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new ListStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    protected ListStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createList(ctx.getArgument(), substatements);
    }

    @Override
    protected ListStatement attachDeclarationReference(final ListStatement stmt, final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateList(stmt, reference);
    }

    @Override
    public ListEffectiveStatement copyEffective(final Current<QName, ListStatement> stmt,
            final ListEffectiveStatement original) {
        return EffectiveStatements.copyList(original, stmt.getArgument(),
            computeFlags(stmt, original.effectiveSubstatements()));
    }

    @Override
    protected ListEffectiveStatement createEffective(final Current<QName, ListStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
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
                    throw stmt.newInferenceException("Key '%s' misses node '%s' in list '%s'",
                        keyStmt.getDeclared().rawArgument(), keyQName.getLocalName(), stmt.argument());
                }
                keyDefinitionInit.add(keyQName);
            }

            keyDefinition = ImmutableList.copyOf(keyDefinitionInit);
        } else {
            keyDefinition = ImmutableList.of();
        }

        final int flags = computeFlags(stmt, substatements);
        if (warnForUnkeyedLists && stmt.effectiveConfig() == EffectiveConfig.TRUE
                && keyDefinition.isEmpty() && isInstantied(stmt)) {
            warnConfigList(stmt);
        }

        EffectiveStmtUtils.checkUniqueGroupings(stmt, substatements);
        EffectiveStmtUtils.checkUniqueTypedefs(stmt, substatements);
        EffectiveStmtUtils.checkUniqueUses(stmt, substatements);

        try {
            return EffectiveStatements.createList(stmt.declared(), stmt.getArgument(), flags, substatements,
                keyDefinition, EffectiveStmtUtils.createElementCountConstraint(substatements).orElse(null));
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    public EffectiveStatementState extractEffectiveState(final ListEffectiveStatement stmt) {
        verify(stmt instanceof ListSchemaNode, "Unexpected statement %s", stmt);
        final var schema = (ListSchemaNode) stmt;
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

    private static void warnConfigList(final @NonNull Current<QName, ListStatement> stmt) {
        final StatementSourceReference ref = stmt.sourceReference();
        final Boolean warned = stmt.namespaceItem(ConfigListWarningNamespace.INSTANCE, ref);
        // Hacky check if we have issued a warning for the original statement
        if (warned == null) {
            final var ctx = stmt.caerbannog();
            if (!(ctx instanceof Mutable<?, ?, ?> mutable)) {
                throw new VerifyException("Unexpected context " + ctx);
            }
            mutable.addToNs(ConfigListWarningNamespace.INSTANCE, ref, Boolean.TRUE);
            LOG.info("""
                Configuration list {} does not define any keys in violation of RFC7950 section 7.8.2. While this is \
                fine with OpenDaylight, it can cause interoperability issues with other systems [defined at {}]""",
                stmt.argument(), ref);
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
