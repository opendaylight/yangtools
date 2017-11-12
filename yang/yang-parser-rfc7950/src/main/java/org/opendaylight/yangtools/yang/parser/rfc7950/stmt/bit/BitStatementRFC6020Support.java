/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.bit;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public class BitStatementRFC6020Support extends AbstractBitStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.BIT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.POSITION)
        .build();

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}