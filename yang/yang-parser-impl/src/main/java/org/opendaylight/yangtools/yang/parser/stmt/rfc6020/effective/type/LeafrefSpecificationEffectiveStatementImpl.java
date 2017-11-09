/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PathEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RequireInstanceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.LeafrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class LeafrefSpecificationEffectiveStatementImpl
        extends DeclaredEffectiveStatementBase<String, LeafrefSpecification>
        implements TypeEffectiveStatement<LeafrefSpecification> {
    private final LeafrefTypeDefinition typeDefinition;

    public LeafrefSpecificationEffectiveStatementImpl(final StmtContext<String, LeafrefSpecification,
            EffectiveStatement<String, LeafrefSpecification>> ctx) {
        super(ctx);

        final LeafrefTypeBuilder builder = BaseTypes.leafrefTypeBuilder(ctx.getSchemaPath().get());

        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof PathEffectiveStatement) {
                builder.setPathStatement(((PathEffectiveStatement) stmt).argument());
            } else if (stmt instanceof RequireInstanceEffectiveStatement) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatement)stmt).argument());
            } else if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Nonnull
    @Override
    public LeafrefTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
