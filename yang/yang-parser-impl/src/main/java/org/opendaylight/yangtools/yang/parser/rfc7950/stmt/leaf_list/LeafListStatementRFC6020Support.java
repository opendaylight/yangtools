/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf_list;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class LeafListStatementRFC6020Support extends AbstractLeafListStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .LEAF_LIST)
        .addOptional(YangStmtMapping.CONFIG)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.MIN_ELEMENTS)
        .addOptional(YangStmtMapping.MAX_ELEMENTS)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.ORDERED_BY)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addMandatory(YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.UNITS)
        .addOptional(YangStmtMapping.WHEN)
        .build();

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}