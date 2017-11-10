/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.submodule;

import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class SubmoduleStatementRFC6020Support extends AbstractSubmoduleStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .SUBMODULE)
        .addAny(YangStmtMapping.ANYXML)
        .addAny(YangStmtMapping.AUGMENT)
        .addMandatory(YangStmtMapping.BELONGS_TO)
        .addAny(YangStmtMapping.CHOICE)
        .addOptional(YangStmtMapping.CONTACT)
        .addAny(YangStmtMapping.CONTAINER)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.DEVIATION)
        .addAny(YangStmtMapping.EXTENSION)
        .addAny(YangStmtMapping.FEATURE)
        .addAny(YangStmtMapping.GROUPING)
        .addAny(YangStmtMapping.IDENTITY)
        .addAny(YangStmtMapping.IMPORT)
        .addAny(YangStmtMapping.INCLUDE)
        .addAny(YangStmtMapping.LEAF)
        .addAny(YangStmtMapping.LEAF_LIST)
        .addAny(YangStmtMapping.LIST)
        .addAny(YangStmtMapping.NOTIFICATION)
        .addOptional(YangStmtMapping.ORGANIZATION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addAny(YangStmtMapping.REVISION)
        .addAny(YangStmtMapping.RPC)
        .addAny(YangStmtMapping.TYPEDEF)
        .addAny(YangStmtMapping.USES)
        .addOptional(YangStmtMapping.YANG_VERSION)
        .build();

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}