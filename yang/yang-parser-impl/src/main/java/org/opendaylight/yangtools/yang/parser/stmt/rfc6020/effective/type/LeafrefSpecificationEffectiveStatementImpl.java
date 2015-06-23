/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

import org.opendaylight.yangtools.yang.model.util.Leafref;
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
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PathEffectiveStatementImpl;

public class LeafrefSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, TypeStatement.LeafrefSpecification> implements LeafrefTypeDefinition, TypeDefinitionEffectiveBuilder {

    public static final String LOCAL_NAME = "leafref";
    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, LOCAL_NAME);
    private static final SchemaPath PATH = SchemaPath.create(true, QNAME);
    private static final String DESCRIPTION = "The leafref type is used to reference a particular leaf instance in the data tree.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.9";
    private static final String UNITS = "";

    private RevisionAwareXPath xpath;
    private final SchemaPath path;

    public LeafrefSpecificationEffectiveStatementImpl(StmtContext<String, TypeStatement.LeafrefSpecification, EffectiveStatement<String, TypeStatement.LeafrefSpecification>> ctx) {
        super(ctx);

        path = Utils.getSchemaPath(ctx.getParentContext()).createChild(QNAME);

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof PathEffectiveStatementImpl) {
                xpath = ((PathEffectiveStatementImpl) effectiveStatement).argument();
            }
        }
    }

    @Override
    public RevisionAwareXPath getPathStatement() {
        return xpath;
    }

    @Override
    public LeafrefTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return this;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((xpath == null) ? 0 : xpath.hashCode());
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
        LeafrefSpecificationEffectiveStatementImpl other = (LeafrefSpecificationEffectiveStatementImpl) obj;
        if (xpath == null) {
            if (other.xpath != null) {
                return false;
            }
        } else if (!xpath.equals(other.xpath)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("type ");
        builder.append(QNAME);
        builder.append(" [xpath=");
        builder.append(xpath);
        builder.append("]");
        return builder.toString();
    }

    private Leafref leafrefInstance = null;

    @Override
    public Leafref buildType() {

        if (leafrefInstance != null) {
            return leafrefInstance;
        }
        leafrefInstance = Leafref.create(path, xpath);

        return leafrefInstance;
    }
}
