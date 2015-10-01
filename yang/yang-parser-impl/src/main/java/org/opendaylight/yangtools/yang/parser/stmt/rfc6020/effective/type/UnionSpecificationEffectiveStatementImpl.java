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
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.UnionSpecification;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public class UnionSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, UnionSpecification> implements UnionTypeDefinition,
        TypeDefinitionEffectiveBuilder, DefinitionAwareTypeEffectiveStatement<UnionSpecification, UnionTypeDefinition> {

    private final UnionType type;

    public UnionSpecificationEffectiveStatementImpl(
            final StmtContext<String, UnionSpecification, EffectiveStatement<String, UnionSpecification>> ctx) {
        super(ctx);

        List<TypeDefinition<?>> typesInit = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof TypeDefinition) {
                typesInit.add(TypeUtils.getTypeFromEffectiveStatement(effectiveStatement));
            }
        }

        TypeUtils.sortTypes(typesInit);
        type = UnionType.create(typesInit);
    }

    @Override
    public List<TypeDefinition<?>> getTypes() {
        return type.getTypes();
    }

    @Override
    public UnionTypeDefinition getBaseType() {
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
    public UnionTypeDefinition buildType() {
        return type;
    }

    @Override
    public TypeEffectiveStatement<UnionSpecification> derive(final EffectiveStatement<?, UnionSpecification> stmt,
            final SchemaPath path) {
        return new DerivedEffectiveStatement<UnionSpecification, UnionTypeDefinition>(stmt, path, this);
    }
}
