/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.ImportStatementDefinition;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.SupportedExtensionsMapping;

public class ImportStatementRfc7950Definition extends ImportStatementDefinition {

    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator
            .builder(Rfc6020Mapping.IMPORT)
            .addMandatory(Rfc6020Mapping.PREFIX)
            .addOptional(Rfc6020Mapping.REVISION_DATE)
            .addOptional(SupportedExtensionsMapping.SEMANTIC_VERSION)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .build();

    @Override
    public void onFullDefinitionDeclared(
            final Mutable<String, ImportStatement, EffectiveStatement<String, ImportStatement>> stmt) {
        super.onFullDefinitionDeclared(stmt);
        SUBSTATEMENT_VALIDATOR.validate(stmt);
    }
}
