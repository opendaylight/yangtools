/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
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
import org.opendaylight.yangtools.yang.model.util.IdentityrefType;
import org.opendaylight.yangtools.yang.parser.spi.IdentityNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.BaseEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;

public class IdentityRefSpecificationEffectiveStatementImpl extends
        DeclaredEffectiveStatementBase<String, IdentityRefSpecification> implements IdentityrefTypeDefinition,
        TypeEffectiveStatement<IdentityRefSpecification> {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, TypeUtils.IDENTITY_REF);

    private static final String DESCRIPTION = "The identityref type is used to reference an existing identity.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.10";

    private static final String UNITS = "";

    private final SchemaPath path;

    private final IdentitySchemaNode identity;
    private IdentityrefType identityRefTypeInstance = null;

    public IdentityRefSpecificationEffectiveStatementImpl(
            final StmtContext<String, IdentityRefSpecification, EffectiveStatement<String, IdentityRefSpecification>> ctx) {
        super(ctx);

        path = ctx.getParentContext().getSchemaPath().get().createChild(QNAME);

        final BaseEffectiveStatementImpl base = firstEffective(BaseEffectiveStatementImpl.class);
        if (base != null) {
            QName identityQName = base.argument();
            StmtContext<?, IdentityStatement, EffectiveStatement<QName, IdentityStatement>> identityCtx = ctx.getFromNamespace(IdentityNamespace.class, identityQName);
            identity = (IdentitySchemaNode) identityCtx.buildEffective();
        } else {
            identity = null;
        }
    }

    @Override
    public IdentitySchemaNode getIdentity() {
        return identity;
    }

    @Override
    public IdentityrefTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return identity;
    }

    @Override
    public QName getQName() {
        return QNAME;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getReference() {
        return REFERENCE;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public String toString() {
        return "identityref " + identity.getQName().getLocalName();
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {

        if (identityRefTypeInstance != null) {
            return identityRefTypeInstance;
        }

        identityRefTypeInstance = IdentityrefType.create(path, identity);

        return identityRefTypeInstance;
    }
}
