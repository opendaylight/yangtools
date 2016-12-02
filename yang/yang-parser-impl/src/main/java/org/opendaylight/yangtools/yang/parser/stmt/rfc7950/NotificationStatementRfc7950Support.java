/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.NotificationStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.NotificationEffectiveStatementImpl;

public class NotificationStatementRfc7950Support extends NotificationStatementImpl.Definition {
    // :FIXME add action statement to the set of illegal parents, once bug-6896
    // will be resolved.
    private static final Set<StatementDefinition> ILLEGAL_PARENTS = ImmutableSet.of(YangStmtMapping.NOTIFICATION,
            YangStmtMapping.RPC);

    @Override
    public EffectiveStatement<QName, NotificationStatement> createEffective(
            final StmtContext<QName, NotificationStatement, EffectiveStatement<QName, NotificationStatement>> ctx) {
        SourceException
                .throwIf(StmtContextUtils.hasAncestorOfType(ctx, ILLEGAL_PARENTS), ctx.getStatementSourceReference(),
                        "Notification %s is defined within an rpc, action, or another notification",
                        ctx.getStatementArgument());
        return new NotificationEffectiveStatementImpl(ctx);
    }
}
