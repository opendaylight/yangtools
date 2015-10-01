/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.BitsSpecification;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.BitsType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public class BitsSpecificationEffectiveStatementImpl extends EffectiveStatementBase<String, BitsSpecification>
    implements TypeDefinitionEffectiveBuilder, BitsTypeDefinition,
    DefinitionAwareTypeEffectiveStatement<BitsSpecification, BitsTypeDefinition> {

    private final BitsType type;

    public BitsSpecificationEffectiveStatementImpl(final StmtContext<String, BitsSpecification, EffectiveStatement<String, BitsSpecification>> ctx) {
        super(ctx);

        final List<Bit> bits = new ArrayList<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof Bit) {
                bits.add(((Bit) effectiveStatement));
            }
        }

        type = BitsType.create(Utils.getSchemaPath(ctx.getParentContext()).createChild(BaseTypes.BITS_QNAME), bits);
    }

    @Override
    public List<Bit> getBits() {
        return type.getBits();
    }

    @Override
    public BitsTypeDefinition getBaseType() {
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
    public BitsTypeDefinition buildType() {
        return type;
    }

    @Override
    public TypeEffectiveStatement<BitsSpecification> derive(final EffectiveStatement<?, BitsSpecification> stmt,
            final SchemaPath path) {
        return new DerivedEffectiveStatement<BitsSpecification, BitsTypeDefinition>(stmt, path, this);
    }

    @Override
    public BitsTypeDefinition getTypeSpecificDefinition() {
        return this;
    }
}
