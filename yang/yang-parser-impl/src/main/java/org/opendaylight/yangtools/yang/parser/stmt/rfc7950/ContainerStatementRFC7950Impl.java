/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ContainerStatementImpl;

public class ContainerStatementRFC7950Impl extends ContainerStatementImpl {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .CONTAINER)
            .addAny(Rfc6020Mapping.ANYXML)
            .addAny(Rfc6020Mapping.CHOICE)
            .addOptional(Rfc6020Mapping.CONFIG)
            .addAny(Rfc6020Mapping.CONTAINER)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.GROUPING)
            .addAny(Rfc6020Mapping.IF_FEATURE)
            .addAny(Rfc6020Mapping.LEAF)
            .addAny(Rfc6020Mapping.LEAF_LIST)
            .addAny(Rfc6020Mapping.LIST)
            .addAny(Rfc6020Mapping.MUST)
            .addOptional(Rfc6020Mapping.PRESENCE)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.STATUS)
            .addAny(Rfc6020Mapping.TYPEDEF)
            .addAny(Rfc6020Mapping.USES)
            .addOptional(Rfc6020Mapping.WHEN)
            .addOptional(Rfc6020Mapping.NOTIFICATION)
            .build();

    protected ContainerStatementRFC7950Impl(final StmtContext<QName, ContainerStatement,?> context) {
        super(context);
    }

    public static class Definition extends ContainerStatementImpl.Definition {
        @Override
        public void onFullDefinitionDeclared(final Mutable<QName, ContainerStatement,
                EffectiveStatement<QName, ContainerStatement>> stmt) {
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }
}
