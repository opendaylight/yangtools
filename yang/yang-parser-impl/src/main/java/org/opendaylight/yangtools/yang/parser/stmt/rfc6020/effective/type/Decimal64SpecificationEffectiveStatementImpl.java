/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public class Decimal64SpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, Decimal64Specification>
        implements TypeEffectiveStatement<Decimal64Specification> {

    public Decimal64SpecificationEffectiveStatementImpl(
            final StmtContext<String, Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> ctx) {
        super(ctx);
    }

    @Override
    public TypeDefinitionBuilder<DecimalTypeDefinition> newTypeDefinitionBuilder() {
        return new DecimalTypeDefinitionBuilder();
    }
}
