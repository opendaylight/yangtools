/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class NotificationStatementRFC6020Support extends AbstractNotificationStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(NotificationStatement.DEFINITION)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(DescriptionStatement.DEFINITION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(IfFeatureStatement.DEFINITION)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addOptional(ReferenceStatement.DEFINITION)
            .addOptional(StatusStatement.DEFINITION)
            .addAny(TypedefStatement.DEFINITION)
            .addAny(YangStmtMapping.USES)
            .build();

    public NotificationStatementRFC6020Support(final YangParserConfiguration config) {
        super(config, SUBSTATEMENT_VALIDATOR);
    }
}