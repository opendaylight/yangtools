/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class ContainerStatementSupport
        extends BaseSchemaTreeStatementSupport<ContainerStatement, ContainerEffectiveStatement> {
    private static final @NonNull ContainerStatementSupport RFC6020_INSTANCE = new ContainerStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.CONTAINER)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addOptional(YangStmtMapping.CONFIG)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.MUST)
            .addOptional(YangStmtMapping.PRESENCE)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.WHEN)
            .build());

    private static final @NonNull ContainerStatementSupport RFC7950_INSTANCE = new ContainerStatementSupport(
        SubstatementValidator.builder(YangStmtMapping.CONTAINER)
            .addAny(YangStmtMapping.ACTION)
            .addAny(YangStmtMapping.ANYDATA)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addOptional(YangStmtMapping.CONFIG)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.MUST)
            .addAny(YangStmtMapping.NOTIFICATION)
            .addOptional(YangStmtMapping.PRESENCE)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.WHEN)
            .build());

    private final SubstatementValidator validator;

    private ContainerStatementSupport(final SubstatementValidator validator) {
        super(YangStmtMapping.CONTAINER, instantiatedSchemaTree());
        this.validator = requireNonNull(validator);
    }

    public static @NonNull ContainerStatementSupport rfc6020Instance() {
        return RFC6020_INSTANCE;
    }

    public static @NonNull ContainerStatementSupport rfc7950Instance() {
        return RFC7950_INSTANCE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected ContainerStatement createDeclared(final StmtContext<QName, ContainerStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularContainerStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected ContainerStatement createEmptyDeclared(final StmtContext<QName, ContainerStatement, ?> ctx) {
        return new EmptyContainerStatement(ctx.getArgument());
    }

    @Override
    protected ContainerEffectiveStatement createEffective(final Current<QName, ContainerStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final ContainerSchemaNode original = (ContainerSchemaNode) stmt.original();
        final int flags = new FlagsBuilder()
                .setHistory(stmt.history())
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .setConfiguration(stmt.effectiveConfig().asNullable())
                .setPresence(findFirstStatement(substatements, PresenceEffectiveStatement.class) != null)
                .toFlags();

        EffectiveStmtUtils.checkUniqueGroupings(stmt, substatements);
        EffectiveStmtUtils.checkUniqueTypedefs(stmt, substatements);
        EffectiveStmtUtils.checkUniqueUses(stmt, substatements);

        final SchemaPath path = stmt.wrapSchemaPath();
        try {
            return new ContainerEffectiveStatementImpl(stmt.declared(), substatements, flags, path, original);
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }
}
