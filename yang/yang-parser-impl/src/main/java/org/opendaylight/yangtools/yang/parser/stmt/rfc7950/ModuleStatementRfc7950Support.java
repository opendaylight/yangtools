/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ModuleStatementSupport;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.SupportedExtensionsMapping;

public class ModuleStatementRfc7950Support extends ModuleStatementSupport {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .MODULE)
            .addAny(Rfc6020Mapping.ANYXML)
            .addAny(Rfc6020Mapping.ANYDATA)
            .addAny(Rfc6020Mapping.AUGMENT)
            .addAny(Rfc6020Mapping.CHOICE)
            .addOptional(Rfc6020Mapping.CONTACT)
            .addAny(Rfc6020Mapping.CONTAINER)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.DEVIATION)
            .addAny(Rfc6020Mapping.EXTENSION)
            .addAny(Rfc6020Mapping.FEATURE)
            .addAny(Rfc6020Mapping.GROUPING)
            .addAny(Rfc6020Mapping.IDENTITY)
            .addAny(Rfc6020Mapping.IMPORT)
            .addAny(Rfc6020Mapping.INCLUDE)
            .addAny(Rfc6020Mapping.LEAF)
            .addAny(Rfc6020Mapping.LEAF_LIST)
            .addAny(Rfc6020Mapping.LIST)
            .addMandatory(Rfc6020Mapping.NAMESPACE)
            .addAny(Rfc6020Mapping.NOTIFICATION)
            .addOptional(Rfc6020Mapping.ORGANIZATION)
            .addMandatory(Rfc6020Mapping.PREFIX)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addAny(Rfc6020Mapping.REVISION)
            .addAny(Rfc6020Mapping.RPC)
            .addAny(Rfc6020Mapping.TYPEDEF)
            .addAny(Rfc6020Mapping.USES)
            .addMandatory(Rfc6020Mapping.YANG_VERSION)
            .addOptional(SupportedExtensionsMapping.SEMANTIC_VERSION)
            .build();

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }
}
