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
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.InstanceIdentifierSpecification;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.InstanceIdentifierType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RequireInstanceEffectiveStatementImpl;

public final class InstanceIdentifierSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, InstanceIdentifierSpecification>
        implements TypeDefinitionEffectiveBuilder, InstanceIdentifierTypeDefinition,
        TypeEffectiveStatement<InstanceIdentifierSpecification> {

    private final InstanceIdentifierType type;

    public InstanceIdentifierSpecificationEffectiveStatementImpl(
            final StmtContext<String, InstanceIdentifierSpecification, EffectiveStatement<String, InstanceIdentifierSpecification>> ctx) {
        super(ctx);

        RequireInstanceEffectiveStatementImpl requireInstanceStmtCtx = firstEffective(RequireInstanceEffectiveStatementImpl.class);
        boolean requireInstance = (requireInstanceStmtCtx != null) ? requireInstanceStmtCtx.argument() : false;

        type = InstanceIdentifierType.create(requireInstance);
    }

    @Deprecated
    @Override
    public RevisionAwareXPath getPathStatement() {
        return type.getPathStatement();
    }

    @Override
    public boolean requireInstance() {
        return type.requireInstance();
    }

    @Override
    public InstanceIdentifierTypeDefinition getBaseType() {
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
    public TypeDefinition<?> buildType() {
        return type;
    }

    @Override
    public TypeEffectiveStatement<InstanceIdentifierSpecification> derive(
            final EffectiveStatement<?, InstanceIdentifierSpecification> stmt, final SchemaPath path) {
        return new DerivedInstanceIdentifierEffectiveStatement(stmt, path, this);
    }
}
