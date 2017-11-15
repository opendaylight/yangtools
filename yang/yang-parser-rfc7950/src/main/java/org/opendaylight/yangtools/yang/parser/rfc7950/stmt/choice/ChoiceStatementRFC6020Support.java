/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.choice;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.case_.CaseStatementRFC6020Support;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ChoiceStatementRFC6020Support extends AbstractChoiceStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .CHOICE)
        .addAny(YangStmtMapping.ANYXML)
        .addAny(YangStmtMapping.CASE)
        .addOptional(YangStmtMapping.CONFIG)
        .addAny(YangStmtMapping.CONTAINER)
        .addOptional(YangStmtMapping.DEFAULT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addAny(YangStmtMapping.LEAF)
        .addAny(YangStmtMapping.LEAF_LIST)
        .addAny(YangStmtMapping.LIST)
        .addOptional(YangStmtMapping.MANDATORY)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.WHEN)
        .build();
    private static final ChoiceStatementRFC6020Support INSTANCE = new ChoiceStatementRFC6020Support();

    private ChoiceStatementRFC6020Support() {
        // Hidden
    }

    public static ChoiceStatementRFC6020Support getInstance() {
        return INSTANCE;
    }

    @Override
    StatementSupport<?, ?, ?> implictCase() {
        return CaseStatementRFC6020Support.getInstance();
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}