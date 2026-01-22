/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

/**
 * Class providing necessary support for processing YANG 1.1 Augment statement.
 */
public final class AugmentStatementRFC7950Support extends AbstractAugmentStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator
            .builder(YangStmtMapping.AUGMENT)
            .addAny(ActionStatement.DEFINITION)
            .addAny(AnydataStatement.DEFINITION)
            .addAny(AnyxmlStatement.DEFINITION)
            .addAny(CaseStatement.DEFINITION)
            .addAny(ChoiceStatement.DEFINITION)
            .addAny(ContainerStatement.DEFINITION)
            .addOptional(DescriptionStatement.DEFINITION)
            .addAny(IfFeatureStatement.DEFINITION)
            .addAny(LeafStatement.DEFINITION)
            .addAny(LeafListStatement.DEFINITION)
            .addAny(ListStatement.DEFINITION)
            .addAny(NotificationStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .addOptional(StatusStatement.DEFINITION)
            .addAny(UsesStatement.DEFINITION)
            .addOptional(WhenStatement.DEFINITION)
            .build();

    public AugmentStatementRFC7950Support(final YangParserConfiguration config) {
        super(config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    boolean allowsMandatory(final StmtContext<?, ?, ?> ctx) {
        return ctx.publicDefinition() == YangStmtMapping.AUGMENT && hasWhenSubstatement(ctx);
    }
}
