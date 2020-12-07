/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
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
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractContainerStatementSupport
        extends BaseSchemaTreeStatementSupport<ContainerStatement, ContainerEffectiveStatement> {
    AbstractContainerStatementSupport() {
        super(YangStmtMapping.CONTAINER, CopyPolicy.DECLARED_COPY);
    }

    @Override
    protected final ContainerStatement createDeclared(final StmtContext<QName, ContainerStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularContainerStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected final ContainerStatement createEmptyDeclared(final StmtContext<QName, ContainerStatement, ?> ctx) {
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

    @Override
    public @NonNull boolean copyEffective(final ContainerEffectiveStatement original,
                                          final Current<QName, ContainerStatement> stmt) {
        if (((ContainerEffectiveStatementImpl) original).getPath().equals(stmt.wrapSchemaPath())) {
            return false;
        }
        if (((ContainerEffectiveStatementImpl) original).isAddedByUses() !=
                stmt.history().contains(CopyType.ADDED_BY_USES)) {
            return false;
        }
        if (((ContainerEffectiveStatementImpl) original).isAugmenting() !=
                stmt.history().contains(CopyType.ADDED_BY_AUGMENTATION)) {
            return false;
        }
        if (((ContainerEffectiveStatementImpl) original).effectiveConfig()
                .equals(Optional.ofNullable(stmt.effectiveConfig().asNullable()))) {
            return false;
        }
        return true;
    }
}
