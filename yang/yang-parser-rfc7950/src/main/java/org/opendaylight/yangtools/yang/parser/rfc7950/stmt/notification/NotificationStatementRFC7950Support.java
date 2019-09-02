/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Class providing necessary support for processing YANG 1.1 Notification
 * statement.
 */
@Beta
public final class NotificationStatementRFC7950Support extends AbstractNotificationStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .NOTIFICATION)
            .addAny(YangStmtMapping.ANYDATA)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.MUST)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .build();

    private static final ImmutableSet<StatementDefinition> ILLEGAL_PARENTS = ImmutableSet.of(
            YangStmtMapping.NOTIFICATION, YangStmtMapping.RPC, YangStmtMapping.ACTION);
    private static final NotificationStatementRFC7950Support INSTANCE = new NotificationStatementRFC7950Support();

    private NotificationStatementRFC7950Support() {
        // Hidden
    }

    public static NotificationStatementRFC7950Support getInstance() {
        return INSTANCE;
    }

    @Override
    public EffectiveStatement<QName, NotificationStatement> createEffective(
            final StmtContext<QName, NotificationStatement, EffectiveStatement<QName, NotificationStatement>> ctx) {
        SourceException.throwIf(StmtContextUtils.hasAncestorOfType(ctx, ILLEGAL_PARENTS),
            ctx.getStatementSourceReference(),
            "Notification %s is defined within an rpc, action, or another notification",
            ctx.getStatementArgument());
        SourceException.throwIf(!StmtContextUtils.hasAncestorOfTypeWithChildOfType(ctx, YangStmtMapping.LIST,
            YangStmtMapping.KEY), ctx.getStatementSourceReference(),
            "Notification %s is defined within a list that has no key statement", ctx.getStatementArgument());
        SourceException.throwIf(StmtContextUtils.hasParentOfType(ctx, YangStmtMapping.CASE),
            ctx.getStatementSourceReference(), "Notification %s is defined within a case statement",
            ctx.getStatementArgument());
        return new NotificationEffectiveStatementImpl(ctx);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
