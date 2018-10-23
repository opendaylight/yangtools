/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.jdt.annotation.NonNull;
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

@VisibleForTesting
// FIXME: hide this class
public final class LeafrefSpecificationEffectiveStatement
        extends DeclaredEffectiveStatementBase<String, LeafrefSpecification>
        implements TypeEffectiveStatement<LeafrefSpecification> {
    private final @NonNull LeafrefTypeDefinition typeDefinition;

    LeafrefSpecificationEffectiveStatement(final StmtContext<String, LeafrefSpecification,
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

    @Override
    public LeafrefTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
