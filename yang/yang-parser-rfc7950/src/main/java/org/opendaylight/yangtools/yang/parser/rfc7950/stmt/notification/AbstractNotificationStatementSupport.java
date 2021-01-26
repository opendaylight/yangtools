/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractNotificationStatementSupport
        extends BaseSchemaTreeStatementSupport<NotificationStatement, NotificationEffectiveStatement> {
    AbstractNotificationStatementSupport() {
        super(YangStmtMapping.NOTIFICATION, uninstantiatedPolicy());
    }

    @Override
    protected final NotificationStatement createDeclared(final StmtContext<QName, NotificationStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularNotificationStatement(ctx.getArgument(), substatements);
    }

    @Override
    protected final NotificationStatement createEmptyDeclared(final StmtContext<QName, NotificationStatement, ?> ctx) {
        return new EmptyNotificationStatement(ctx.getArgument());
    }

    @Override
    protected final NotificationEffectiveStatement createEffective(final Current<QName, NotificationStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return new NotificationEffectiveStatementImpl(stmt.declared(), substatements,
                EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), substatements), stmt.wrapSchemaPath());
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    public final NotificationEffectiveStatement copyEffective(final Current<QName, NotificationStatement> stmt,
            final NotificationEffectiveStatement original) {
        return new NotificationEffectiveStatementImpl((NotificationEffectiveStatementImpl) original,
            EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), original.effectiveSubstatements()),
            stmt.wrapSchemaPath());
    }
}