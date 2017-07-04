/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.LeafrefSpecificationImpl;

/**
 * Class providing necessary support for processing YANG 1.1 leafref statement.
 */
@Beta
public class LeafrefSpecificationRfc7950Support extends LeafrefSpecificationImpl.Definition {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPE)
            .addMandatory(YangStmtMapping.PATH)
            .addOptional(YangStmtMapping.REQUIRE_INSTANCE)
            .build();

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

}
