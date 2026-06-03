/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

/**
 * Class providing necessary support for processing YANG 1.1 Augment statement.
 */
public final class AugmentStatementRFC7950Support extends AbstractAugmentStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator
            .builder(YangStmtMapping.AUGMENT)
            .addAny(YangStmtMapping.ACTION)
            .addAny(YangStmtMapping.ANYDATA)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CASE)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.NOTIFICATION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.WHEN)
            .build();

    public AugmentStatementRFC7950Support(final YangParserConfiguration config) {
        super(config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    MandatoryNodesAllowed mandatoryNodesAllowed(
            final StmtContext<SchemaNodeIdentifier, AugmentStatement, AugmentEffectiveStatement> stmt) {
        // RFC7950, page 120:
        //    If the augmentation adds mandatory nodes (see Section 3) that
        //    represent configuration to a target node in another module, the
        //    augmentation MUST be made conditional with a "when" statement.
        return stmt.hasSubstatement(WhenEffectiveStatement.class) ? MandatoryNodesAllowed.ALWAYS
            : MandatoryNodesAllowed.NON_CONFIG;
    }
}
