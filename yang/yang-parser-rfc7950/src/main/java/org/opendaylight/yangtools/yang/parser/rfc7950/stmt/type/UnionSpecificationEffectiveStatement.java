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
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.UnionSpecification;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@VisibleForTesting
// FIXME: hide this class
public final class UnionSpecificationEffectiveStatement extends
        DeclaredEffectiveStatementBase<String, UnionSpecification> implements
        TypeEffectiveStatement<UnionSpecification> {

    private final @NonNull UnionTypeDefinition typeDefinition;

    UnionSpecificationEffectiveStatement(
            final StmtContext<String, UnionSpecification, EffectiveStatement<String, UnionSpecification>> ctx) {
        super(ctx);

        final UnionTypeBuilder builder = BaseTypes.unionTypeBuilder(ctx.getSchemaPath().get());

        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof TypeEffectiveStatement) {
                builder.addType(((TypeEffectiveStatement<?>)stmt).getTypeDefinition());
            }
            if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Override
    public UnionTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
