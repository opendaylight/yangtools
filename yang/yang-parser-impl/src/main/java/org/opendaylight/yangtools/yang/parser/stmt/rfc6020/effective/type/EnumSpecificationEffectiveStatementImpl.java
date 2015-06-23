/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class EnumSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, TypeStatement.EnumSpecification> implements EnumTypeDefinition, TypeDefinitionEffectiveBuilder {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, "enumeration");

    private static final String DESCRIPTION = "The enumeration built-in type represents values from a set of assigned names.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.6";
    private static final String UNITS = "";

    private final SchemaPath path;
    private EnumPair defaultEnum;
    private final List<EnumPair> enums;

    public EnumSpecificationEffectiveStatementImpl(StmtContext<String, TypeStatement.EnumSpecification, EffectiveStatement<String, TypeStatement.EnumSpecification>> ctx) {
        super(ctx);

        List<EnumPair> enumsInit = new ArrayList<>();

        path = Utils.getSchemaPath(ctx.getParentContext()).createChild(QNAME);

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof EnumPair) {
                enumsInit.add((EnumPair) effectiveStatement);
            }
        }

        defaultEnum = null; // TODO get from parentContext
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
        result = prime * result + ((defaultEnum == null) ? 0 : defaultEnum.hashCode());
        result = prime * result + ((enums == null) ? 0 : enums.hashCode());
        result = prime * result + QNAME.hashCode();
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        if (defaultEnum == null) {
            if (other.defaultEnum != null) {
                return false;
            }
        } else if (!defaultEnum.equals(other.defaultEnum)) {
            return false;
        }
        if (enums == null) {
            if (other.enums != null) {
                return false;
            }
        } else if (!enums.equals(other.enums)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
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

    private EnumerationType enumerationTypeInstance = null;
    @Override
    public TypeDefinition<?> buildType() {

        if(enumerationTypeInstance !=null) {
            return enumerationTypeInstance;
        }

        //:FIXME set defaultValue as parameter
        enumerationTypeInstance = EnumerationType.create(path, enums, Optional.<EnumPair>absent());

        return enumerationTypeInstance;
    }
}
