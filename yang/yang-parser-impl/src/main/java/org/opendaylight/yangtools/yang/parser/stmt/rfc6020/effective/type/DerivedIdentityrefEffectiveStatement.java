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
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.BaseEffectiveStatementImpl;

final class DerivedIdentityrefEffectiveStatement extends AbstractTypeEffectiveStatement<IdentityRefSpecification, IdentityrefTypeDefinition>
        implements IdentityrefTypeDefinition {
    private final IdentitySchemaNode identity;

    DerivedIdentityrefEffectiveStatement(final EffectiveStatement<?, IdentityRefSpecification> stmt, final SchemaPath path,
            final IdentityrefTypeDefinition baseType) {
        super(stmt, path, baseType);

        final BaseEffectiveStatementImpl base = findEffective(stmt, BaseEffectiveStatementImpl.class);
        if (base != null) {
            StmtContext<?, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> identityCtx =
                    stmt.get(IdentityNamespace.class, base.argument());
            identity = (IdentitySchemaNode) identityCtx.buildEffective();
        } else {
            identity = baseType.getIdentity();
        }
    }

    @Override
    public TypeEffectiveStatement<IdentityRefSpecification> derive(final EffectiveStatement<?, IdentityRefSpecification> stmt,
            final SchemaPath path) {
        return new DerivedIdentityrefEffectiveStatement(stmt, path, this);
    }

    @Override
    public IdentitySchemaNode getIdentity() {
        return identity;
    }
}
