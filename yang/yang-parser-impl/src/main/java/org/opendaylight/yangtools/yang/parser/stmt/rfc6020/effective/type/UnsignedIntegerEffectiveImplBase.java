/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Optional;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

abstract class UnsignedIntegerEffectiveImplBase extends
        EffectiveStatementBase<String, TypeStatement> implements UnsignedIntegerTypeDefinition {
    private static final long serialVersionUID = 1L;

    private static final String REFERENCE_INT = "https://tools.ietf.org/html/rfc6020#section-9.2";

    protected static final Number MIN_RANGE = 0;
    private final QName qName;
    private final SchemaPath path;
    private final String units = "";
    private final String description;
    private final List<RangeConstraint> rangeStatements;

    protected UnsignedIntegerEffectiveImplBase(final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final String localName, final Number maxRange, final String description) {

        super(ctx);

        this.qName = QName.create(YangConstants.RFC6020_YANG_MODULE, localName);
        path = Utils.getSchemaPath(ctx);

        final String rangeDescription = "Integer values between " + MIN_RANGE + " and " + maxRange + ", inclusively.";
        final RangeConstraint defaultRange = new RangeConstraintEffectiveImpl(MIN_RANGE, maxRange,
                Optional.of(rangeDescription), Optional.of(RangeConstraintEffectiveImpl.DEFAULT_REFERENCE));
        rangeStatements = Collections.singletonList(defaultRange);

        this.description = description;
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return rangeStatements;
    }

    @Override
    public UnsignedIntegerTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return units;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public QName getQName() {
        return qName;
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
        return description;
    }

    @Override
    public String getReference() {
        return REFERENCE_INT;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(qName);
        result = prime * result + Objects.hashCode(path);
        result = prime * result + Objects.hashCode(rangeStatements);
        result = prime * result + Objects.hashCode(units);
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
        UnsignedIntegerEffectiveImplBase other = (UnsignedIntegerEffectiveImplBase) obj;
        if (!Objects.equals(description, other.description)) {
            return false;
        }
        if (!Objects.equals(qName, other.qName)) {
            return false;
        }
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        if (!Objects.equals(rangeStatements, other.rangeStatements)) {
            return false;
        }
        if (!Objects.equals(units, other.units)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(" [name=");
        builder.append(qName);
        builder.append(", path=");
        builder.append(path);
        builder.append(", description=");
        builder.append(description);
        builder.append(", reference=");
        builder.append(REFERENCE_INT);
        builder.append(", units=");
        builder.append(units);
        builder.append(", rangeStatements=");
        builder.append(rangeStatements);
        builder.append("]");
        return builder.toString();
    }
}
