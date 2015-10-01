/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IdentityStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.IdentityrefType;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.BaseEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public final class IdentityRefSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, IdentityRefSpecification> implements IdentityrefTypeDefinition,
        TypeDefinitionEffectiveBuilder,
        DefinitionAwareTypeEffectiveStatement<IdentityRefSpecification, IdentityrefTypeDefinition> {
    private final IdentityrefType type;

    public IdentityRefSpecificationEffectiveStatementImpl(
            final StmtContext<String, IdentityRefSpecification, EffectiveStatement<String, IdentityRefSpecification>> ctx) {
        super(ctx);

        final SchemaPath path = Utils.getSchemaPath(ctx.getParentContext()).createChild(BaseTypes.IDENTITYREF_QNAME);
        final IdentitySchemaNode identity;
        final BaseEffectiveStatementImpl base = firstEffective(BaseEffectiveStatementImpl.class);
        if (base != null) {
            QName identityQName = base.argument();
            StmtContext<?, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> identityCtx =
                    ctx.getFromNamespace(IdentityNamespace.class, identityQName);
            identity = (IdentitySchemaNode) identityCtx.buildEffective();
        } else {
            identity = null;
        }

        type = IdentityrefType.create(path, identity);
    }

    @Override
    public IdentitySchemaNode getIdentity() {
        return type.getIdentity();
    }

    @Override
    public IdentityrefTypeDefinition getBaseType() {
        return type.getBaseType();
    }

    @Override
    public String getUnits() {
        return type.getUnits();
    }

    @Override
    public Object getDefaultValue() {
        return type.getDefaultValue();
    }

    @Override
    public QName getQName() {
        return type.getQName();
    }

    @Override
    public SchemaPath getPath() {
        return type.getPath();
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return type.getUnknownSchemaNodes();
    }

    @Override
    public String getDescription() {
        return type.getDescription();
    }

    @Override
    public String getReference() {
        return type.getReference();
    }

    @Override
    public Status getStatus() {
        return type.getStatus();
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public TypeDefinition<?> buildType() {
        return type;
    }

    @Override
    public TypeEffectiveStatement<IdentityRefSpecification> derive(final EffectiveStatement<?, IdentityRefSpecification> stmt,
            final SchemaPath path) {
        return new DerivedIdentityrefEffectiveStatement(stmt, path, this);
    }
}
