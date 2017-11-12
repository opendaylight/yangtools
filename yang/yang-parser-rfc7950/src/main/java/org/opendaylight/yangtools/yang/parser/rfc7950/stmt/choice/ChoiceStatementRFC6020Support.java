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
    // FIXME: share instance
    private static final StatementSupport<?, ?, ?> IMPLICIT_CASE = new CaseStatementRFC6020Support();
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

    @Override
    StatementSupport<?, ?, ?> implictCase() {
        return IMPLICIT_CASE;
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}