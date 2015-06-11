/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.util.InstanceIdentifierType;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.RequireInstanceEffectiveStatementImpl;

public class InstanceIdentifierSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, TypeStatement.InstanceIdentifierSpecification> implements TypeDefinitionEffectiveBuilder, InstanceIdentifierTypeDefinition {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, "instance-identifier");
    private static final SchemaPath PATH = SchemaPath.create(true, QNAME);
    private static final String DESCRIPTION = "The instance-identifier built-in type is used to "
            + "uniquely identify a particular instance node in the data tree.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.13";

    private static final String UNITS = "";
    private final Boolean requireInstance;

    public InstanceIdentifierSpecificationEffectiveStatementImpl(
            StmtContext<String, TypeStatement.InstanceIdentifierSpecification, EffectiveStatement<String, TypeStatement.InstanceIdentifierSpecification>> ctx) {
        super(ctx);

        RequireInstanceEffectiveStatementImpl requireInstanceStmtCtx = firstEffective(RequireInstanceEffectiveStatementImpl.class);
        requireInstance = (requireInstanceStmtCtx != null) ? requireInstanceStmtCtx.argument() : false;
    }

    @Override
    public RevisionAwareXPath getPathStatement() {
        return null;
    }

    @Override
    public boolean requireInstance() {
        return requireInstance;
    }

    @Override
    public InstanceIdentifierTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public QName getQName() {
        return QNAME;
    }

    @Override
    public SchemaPath getPath() {
        return PATH;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + requireInstance.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        InstanceIdentifierSpecificationEffectiveStatementImpl other = (InstanceIdentifierSpecificationEffectiveStatementImpl) obj;
        return requireInstance.equals(other.requireInstance);
    }

    @Override
    public TypeDefinition<?> buildType() {
        return InstanceIdentifierType.create(requireInstance);
    }
}
