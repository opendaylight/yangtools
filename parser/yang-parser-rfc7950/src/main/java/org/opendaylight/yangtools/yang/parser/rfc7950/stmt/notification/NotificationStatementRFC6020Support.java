/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.notification;

import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class NotificationStatementRFC6020Support extends AbstractNotificationStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(NotificationStatement.DEFINITION)
            .addAny(AnyxmlStatement.DEFINITION)
            .addAny(ChoiceStatement.DEFINITION)
            .addAny(ContainerStatement.DEFINITION)
            .addOptional(DescriptionStatement.DEFINITION)
            .addAny(GroupingStatement.DEFINITION)
            .addAny(IfFeatureStatement.DEFINITION)
            .addAny(LeafStatement.DEFINITION)
            .addAny(LeafListStatement.DEFINITION)
            .addAny(ListStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .addOptional(StatusStatement.DEFINITION)
            .addAny(TypedefStatement.DEFINITION)
            .addAny(UsesStatement.DEFINITION)
            .build();

    public NotificationStatementRFC6020Support(final YangParserConfiguration config) {
        super(config, SUBSTATEMENT_VALIDATOR);
    }
}