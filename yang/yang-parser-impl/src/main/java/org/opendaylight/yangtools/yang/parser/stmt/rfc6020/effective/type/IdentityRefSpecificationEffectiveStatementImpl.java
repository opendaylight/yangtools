/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.IdentityrefType;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.BaseEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public final class IdentityRefSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, IdentityRefSpecification> implements
        TypeEffectiveStatement<IdentityRefSpecification> {

    public IdentityRefSpecificationEffectiveStatementImpl(final StmtContext<String, IdentityRefSpecification, ?> ctx) {
        super(ctx);
    }

    @Override
    public TypeDefinitionBuilder<IdentityrefTypeDefinition> newTypeDefinitionBuilder() {
        return new AbstractTypeDefinitionBuilder<IdentityrefTypeDefinition>() {
            private IdentitySchemaNode identity;

            @Override
            protected void addEffectiveStatement(final EffectiveStatement<?, ?> stmt) {
                if (stmt instanceof BaseEffectiveStatementImpl) {
                    StmtContext<?, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> identityCtx =
                            stmt.get(IdentityNamespace.class, ((BaseEffectiveStatementImpl) stmt).argument());
                    identity = (IdentitySchemaNode) identityCtx.buildEffective();
                };
            }

            @Override
            public IdentityrefTypeDefinition build() {
                // FIXME: not quite right:
                return IdentityrefType.create(getPath(), identity);
            }
        };
    }
}
