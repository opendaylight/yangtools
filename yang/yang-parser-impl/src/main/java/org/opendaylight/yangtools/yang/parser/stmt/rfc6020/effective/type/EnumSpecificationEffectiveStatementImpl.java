/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.EnumSpecification;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public class EnumSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, EnumSpecification> implements EnumTypeDefinition, TypeEffectiveStatement<EnumSpecification> {
    private static final long serialVersionUID = 1L;

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, "enumeration");

    private static final String DESCRIPTION = "The enumeration built-in type represents values from a set of assigned names.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.6";
    private static final String UNITS = "";

    private final SchemaPath path;
    private final EnumPair defaultEnum;
    private final List<EnumPair> enums;
    private EnumerationType enumerationTypeInstance = null;

    public EnumSpecificationEffectiveStatementImpl(final StmtContext<String, EnumSpecification, EffectiveStatement<String, EnumSpecification>> ctx) {
        super(ctx);

        List<EnumPair> enumsInit = new ArrayList<>();

        path = Utils.getSchemaPath(ctx.getParentContext()).createChild(QNAME);

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof EnumPair) {
                enumsInit.add((EnumPair) effectiveStatement);
            }
        }

        // FIXME: get from parentContext
        defaultEnum = null;
        enums = ImmutableList.copyOf(enumsInit);
    }

    @Override
    public List<EnumPair> getValues() {
        return enums;
    }

    @Override
    public EnumTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return defaultEnum;
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
        result = prime * result + Objects.hashCode(defaultEnum);
        result = prime * result + Objects.hashCode(enums);
        result = prime * result + QNAME.hashCode();
        result = prime * result + Objects.hashCode(path);
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
        EnumSpecificationEffectiveStatementImpl other = (EnumSpecificationEffectiveStatementImpl) obj;
        if (!Objects.equals(defaultEnum, other.defaultEnum)) {
            return false;
        }
        if (!Objects.equals(enums, other.enums)) {
            return false;
        }
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(EnumSpecificationEffectiveStatementImpl.class.getSimpleName());
        builder.append(" [name=");
        builder.append(QNAME);
        builder.append(", path=");
        builder.append(path);
        builder.append(", description=");
        builder.append(DESCRIPTION);
        builder.append(", reference=");
        builder.append(REFERENCE);
        builder.append(", defaultEnum=");
        builder.append(defaultEnum);
        builder.append(", enums=");
        builder.append(enums);
        builder.append(", units=");
        builder.append(UNITS);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {

        if (enumerationTypeInstance !=null) {
            return enumerationTypeInstance;
        }

        // FIXME: set defaultValue as parameter
        enumerationTypeInstance = EnumerationType.create(path, enums, Optional.<EnumPair>absent());

        return enumerationTypeInstance;
    }
}
