/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public final class EnumSpecificationEffectiveStatementImpl extends EffectiveStatementBase<String, EnumSpecification>
        implements EnumTypeDefinition, TypeDefinitionEffectiveBuilder,
        DefinitionAwareTypeEffectiveStatement<EnumSpecification, EnumTypeDefinition> {
    private final EnumerationType type;

    public EnumSpecificationEffectiveStatementImpl(final StmtContext<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>> ctx) {
        super(ctx);

        final List<EnumPair> enums = new ArrayList<>();
        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof EnumPair) {
                enums.add((EnumPair) effectiveStatement);
            }
        }

        // We do not need to look the default statement up, as we are executing in the context of the definition itself,
        // the default type will be wrapped by the defining typedef statement.
        type = EnumerationType.create(Utils.getSchemaPath(ctx.getParentContext()).createChild(BaseTypes.ENUMERATION_QNAME),
            enums, Optional.<EnumPair>absent());
    }

    @Override
    public List<EnumPair> getValues() {
        return type.getValues();
    }

    @Override
    public EnumTypeDefinition getBaseType() {
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
    public TypeEffectiveStatement<EnumSpecification> derive(final EffectiveStatement<?, EnumSpecification> stmt,
            final SchemaPath path) {
        return new DerivedEffectiveStatement<EnumSpecification, EnumTypeDefinition>(stmt, path, this);
    }

    @Override
    public EnumTypeDefinition getTypeSpecificDefinition() {
        return this;
    }
}
