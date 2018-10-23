/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.InstanceIdentifierTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.RestrictedTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class InstanceIdentifierSpecificationEffectiveStatement
        extends DeclaredEffectiveStatementBase<String, InstanceIdentifierSpecification>
        implements TypeEffectiveStatement<InstanceIdentifierSpecification> {

    private final @NonNull InstanceIdentifierTypeDefinition typeDefinition;

    InstanceIdentifierSpecificationEffectiveStatement(final StmtContext<String,
            InstanceIdentifierSpecification, EffectiveStatement<String, InstanceIdentifierSpecification>> ctx) {
        super(ctx);

        final InstanceIdentifierTypeBuilder builder = RestrictedTypes.newInstanceIdentifierBuilder(
            BaseTypes.instanceIdentifierType(), ctx.getSchemaPath().get());

        for (EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement)stmt).argument());
            }
            if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Override
    public InstanceIdentifierTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
