/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ImportStatementRFC6020Support extends AbstractImportStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator
            .builder(YangStmtMapping.IMPORT)
            .addMandatory(YangStmtMapping.PREFIX)
            .addOptional(YangStmtMapping.REVISION_DATE)
            .addOptional(OpenConfigStatements.OPENCONFIG_VERSION)
            .build();

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
