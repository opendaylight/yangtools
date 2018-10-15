/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.tailf.common.parser;

import org.opendaylight.yangtools.tailf.common.model.api.TailFCommonStatements;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.action.AbstractActionStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

abstract class AbstractTailFActionStatementSupport extends AbstractActionStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.ACTION)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.INPUT)
        .addOptional(YangStmtMapping.OUTPUT)
        .addOptional(YangStmtMapping.STATUS)
        .addAny(YangStmtMapping.TYPEDEF)
        .build();

    AbstractTailFActionStatementSupport() {
        super(TailFCommonStatements.ACTION);
    }

    @Override
    protected final SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
