/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class AugmentStatementRFC6020Support extends AbstractAugmentStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(AugmentStatement.DEFINITION)
            .addAny(AnyxmlStatement.DEFINITION)
            .addAny(YangStmtMapping.CASE)
            .addAny(YangStmtMapping.CHOICE)
            .addAny(ContainerStatement.DEFINITION)
            .addOptional(DescriptionStatement.DEFINITION)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(LeafStatement.DEFINITION)
            .addAny(LeafListStatement.DEFINITION)
            .addAny(ListStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.WHEN)
            .build();

    public AugmentStatementRFC6020Support(final YangParserConfiguration config) {
        super(config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    boolean allowsMandatory(final StmtContext<?, ?, ?> ctx) {
        return false;
    }
}