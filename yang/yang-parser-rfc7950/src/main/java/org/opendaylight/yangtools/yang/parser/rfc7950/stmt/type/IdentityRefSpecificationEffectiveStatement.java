/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.BaseEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

// FIXME: hide this class
public final class IdentityRefSpecificationEffectiveStatement extends
        DeclaredEffectiveStatementBase<String, IdentityRefSpecification> implements
        TypeEffectiveStatement<IdentityRefSpecification> {

    private final @NonNull IdentityrefTypeDefinition typeDefinition;

    IdentityRefSpecificationEffectiveStatement(final StmtContext<String, IdentityRefSpecification,
            EffectiveStatement<String, IdentityRefSpecification>> ctx) {
        super(ctx);

        final IdentityrefTypeBuilder builder = BaseTypes.identityrefTypeBuilder(ctx.getSchemaPath().get());
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof BaseEffectiveStatement) {
                final QName identityQName = ((BaseEffectiveStatement) stmt).argument();
                final StmtContext<?, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> identityCtx =
                        ctx.getFromNamespace(IdentityNamespace.class, identityQName);
                builder.addIdentity((IdentitySchemaNode) identityCtx.buildEffective());
            }
            if (stmt instanceof UnknownSchemaNode) {
                builder.addUnknownSchemaNode((UnknownSchemaNode)stmt);
            }
        }

        typeDefinition = builder.build();
    }

    @Override
    public IdentityrefTypeDefinition getTypeDefinition() {
        return typeDefinition;
    }
}
