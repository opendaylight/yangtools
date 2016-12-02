/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ContainerStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc7950.effective.ContainerEffectiveStatementRfc7950Impl;

public class ContainerStatementRfc7950Impl extends ContainerStatementImpl {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .CONTAINER)
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
            .build();

    protected ContainerStatementRfc7950Impl(final StmtContext<QName, ContainerStatement, ?> context) {
        super(context);
    }

    public static class Definition extends ContainerStatementImpl.Definition {
        @Override
        public ContainerStatement createDeclared(final StmtContext<QName, ContainerStatement, ?> ctx) {
            return new ContainerStatementRfc7950Impl(ctx);
        }

        @Override
        public EffectiveStatement<QName, ContainerStatement> createEffective(
                final StmtContext<QName, ContainerStatement, EffectiveStatement<QName, ContainerStatement>> ctx) {
            return new ContainerEffectiveStatementRfc7950Impl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Override
    public final Collection<? extends NotificationStatement> getNotifications() {
        return allDeclared(NotificationStatement.class);
    }
}
