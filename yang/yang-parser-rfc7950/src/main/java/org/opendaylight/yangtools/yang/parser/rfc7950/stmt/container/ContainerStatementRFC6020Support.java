/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.container;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ContainerStatementRFC6020Support extends AbstractContainerStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.CONTAINER)
        .addAny(YangStmtMapping.ANYXML)
        .addAny(YangStmtMapping.CHOICE)
        .addOptional(YangStmtMapping.CONFIG)
        .addAny(YangStmtMapping.CONTAINER)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addAny(YangStmtMapping.LEAF)
        .addAny(YangStmtMapping.LEAF_LIST)
        .addAny(YangStmtMapping.LIST)
        .addAny(YangStmtMapping.MUST)
        .addOptional(YangStmtMapping.PRESENCE)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addAny(YangStmtMapping.TYPEDEF)
        .addAny(YangStmtMapping.USES)
        .addOptional(YangStmtMapping.WHEN)
        .build();

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}