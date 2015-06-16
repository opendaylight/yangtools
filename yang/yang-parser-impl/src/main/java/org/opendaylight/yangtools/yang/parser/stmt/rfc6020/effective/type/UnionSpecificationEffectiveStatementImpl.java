/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public class UnionSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, TypeStatement.UnionSpecification> implements UnionTypeDefinition,
        TypeDefinitionEffectiveBuilder {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, "union");
    private static final SchemaPath PATH = SchemaPath.create(true, QNAME);
    private static final String DESCRIPTION = "The union built-in type represents a value that corresponds to one of its member types.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.12";

    private final List<TypeDefinition<?>> types;
    private UnionType unionTypeInstance = null;

    public UnionSpecificationEffectiveStatementImpl(
            StmtContext<String, TypeStatement.UnionSpecification, EffectiveStatement<String, TypeStatement.UnionSpecification>> ctx) {
        super(ctx);

        List<TypeDefinition<?>> typesInit = new ArrayList<>();

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof TypeDefinition) {
                typesInit.add(TypeUtils.getTypeFromEffectiveStatement(effectiveStatement));
            }
        }

        TypeUtils.sortTypes(typesInit);

        types = ImmutableList.copyOf(typesInit);
    }

    @Override
    public List<TypeDefinition<?>> getTypes() {
        return types;
    }

    @Override
    public UnionTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return null;
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
        result = prime * result + types.hashCode();
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
        UnionSpecificationEffectiveStatementImpl other = (UnionSpecificationEffectiveStatementImpl) obj;
        return types.equals(other.types);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("type ");
        builder.append(QNAME);
        builder.append(" (types=[");
        for (TypeDefinition<?> td : types) {
            builder.append(", ").append(td.getQName().getLocalName());
        }
        builder.append(']');
        return builder.toString();
    }

    public TypeDefinition<?> buildType() {

        if (unionTypeInstance != null) {
            return unionTypeInstance;
        }

        unionTypeInstance = UnionType.create(types);

        return unionTypeInstance;
    }
}
