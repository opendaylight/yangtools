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
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

abstract class AbstractNotificationStatementSupport
        extends AbstractSchemaTreeStatementSupport<NotificationStatement, NotificationEffectiveStatement> {
    AbstractNotificationStatementSupport() {
        super(YangStmtMapping.NOTIFICATION, uninstantiatedPolicy());
    }

    @Override
    protected final NotificationStatement createDeclared(final StmtContext<QName, NotificationStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createNotification(ctx.getArgument(), substatements);
    }

    @Override
    protected final NotificationStatement createEmptyDeclared(final StmtContext<QName, NotificationStatement, ?> ctx) {
        return DeclaredStatements.createNotification(ctx.getArgument());
    }

    @Override
    protected final NotificationEffectiveStatement createEffective(final Current<QName, NotificationStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        try {
            return new NotificationEffectiveStatementImpl(stmt.declared(), substatements, stmt.effectivePath(),
                EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), substatements));
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    public final NotificationEffectiveStatement copyEffective(final Current<QName, NotificationStatement> stmt,
            final NotificationEffectiveStatement original) {
        return new NotificationEffectiveStatementImpl((NotificationEffectiveStatementImpl) original,
            stmt.effectivePath(),
            EffectiveStatementMixins.historyAndStatusFlags(stmt.history(), original.effectiveSubstatements()));
    }
}