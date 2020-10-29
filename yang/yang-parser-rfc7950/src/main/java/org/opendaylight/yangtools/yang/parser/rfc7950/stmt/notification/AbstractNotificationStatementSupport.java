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
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

abstract class AbstractNotificationStatementSupport
        extends BaseSchemaTreeStatementSupport<NotificationStatement, NotificationEffectiveStatement> {
    AbstractNotificationStatementSupport() {
        super(YangStmtMapping.NOTIFICATION);
    }

    @Override
    public final QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    protected final NotificationStatement createDeclared(final StmtContext<QName, NotificationStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularNotificationStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected final NotificationStatement createEmptyDeclared(final StmtContext<QName, NotificationStatement, ?> ctx) {
        return new EmptyNotificationStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected final NotificationEffectiveStatement createEffective(
            final StmtContext<QName, NotificationStatement, NotificationEffectiveStatement> ctx,
            final NotificationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        checkEffective(ctx);

        return new NotificationEffectiveStatementImpl(declared, historyAndStatusFlags(ctx, substatements), ctx,
            substatements);
    }

    @Override
    protected final NotificationEffectiveStatement createEmptyEffective(
            final StmtContext<QName, NotificationStatement, NotificationEffectiveStatement> ctx,
            final NotificationStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }

    abstract void checkEffective(StmtContext<QName, NotificationStatement, NotificationEffectiveStatement> ctx);
}