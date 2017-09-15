/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.LeafrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PathEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RequireInstanceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementImpl;

public final class LeafrefSpecificationEffectiveStatementImpl
        extends DeclaredEffectiveStatementBase<String, LeafrefSpecification>
        implements TypeEffectiveStatement<LeafrefSpecification> {
    private final LeafrefTypeDefinition typeDefinition;

    public LeafrefSpecificationEffectiveStatementImpl(final StmtContext<String, LeafrefSpecification,
            EffectiveStatement<String, LeafrefSpecification>> ctx) {
        super(ctx);

        final LeafrefTypeBuilder builder = BaseTypes.leafrefTypeBuilder(ctx.getSchemaPath().get());

        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof PathEffectiveStatementImpl) {
                builder.setPathStatement(((PathEffectiveStatementImpl) stmt).argument());
            } else if (stmt instanceof RequireInstanceEffectiveStatementImpl) {
                builder.setRequireInstance(((RequireInstanceEffectiveStatementImpl)stmt).argument());
            } else if (stmt instanceof UnknownEffectiveStatementImpl) {
                builder.addUnknownSchemaNode((UnknownEffectiveStatementImpl)stmt);
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
