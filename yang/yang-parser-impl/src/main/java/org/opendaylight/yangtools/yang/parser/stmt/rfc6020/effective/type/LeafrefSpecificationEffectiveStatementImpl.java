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
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.LeafrefSpecification;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.Leafref;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.PathEffectiveStatementImpl;

public final class LeafrefSpecificationEffectiveStatementImpl extends EffectiveStatementBase<String, LeafrefSpecification>
        implements LeafrefTypeDefinition, TypeDefinitionEffectiveBuilder,
        DefinitionAwareTypeEffectiveStatement<LeafrefSpecification, LeafrefTypeDefinition> {

    private final Leafref type;

    public LeafrefSpecificationEffectiveStatementImpl(final StmtContext<String, LeafrefSpecification, EffectiveStatement<String, LeafrefSpecification>> ctx) {
        super(ctx);

        final SchemaPath path = Utils.getSchemaPath(ctx.getParentContext()).createChild(BaseTypes.LEAFREF_QNAME);
        RevisionAwareXPath xpath = null;
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof PathEffectiveStatementImpl) {
                xpath = ((PathEffectiveStatementImpl) effectiveStatement).argument();
            }
        }

        type = Leafref.create(path, xpath);
    }

    @Override
    public RevisionAwareXPath getPathStatement() {
        return type.getPathStatement();
    }

    @Override
    public LeafrefTypeDefinition getBaseType() {
        return type.getBaseType();
    }

    @Override
    public String getUnits() {
        return type.getUnits();
    }

    @Override
    public Object getDefaultValue() {
        return this;
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
    public Leafref buildType() {
        return type;
    }

    @Override
    public TypeEffectiveStatement<LeafrefSpecification> derive(final EffectiveStatement<?, LeafrefSpecification> stmt,
            final SchemaPath path) {
        return new DerivedEffectiveStatement<LeafrefSpecification, LeafrefTypeDefinition>(stmt, path, this);
    }
}
