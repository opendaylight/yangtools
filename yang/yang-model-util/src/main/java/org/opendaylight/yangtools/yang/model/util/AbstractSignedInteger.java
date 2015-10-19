/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * The Abstract Integer class defines implementation of IntegerTypeDefinition
 * interface which represents SIGNED Integer values defined in Yang language. <br>
 * The integer built-in types in Yang are int8, int16, int32, int64. They
 * represent signed integers of different sizes:
 *
 * <ul>
 * <li>int8 - represents integer values between -128 and 127, inclusively.</li>
 * <li>int16 - represents integer values between -32768 and 32767, inclusively.</li>
 * <li>int32 - represents integer values between -2147483648 and 2147483647,
 * inclusively.</li>
 * <li>int64 - represents integer values between -9223372036854775808 and
 * 9223372036854775807, inclusively.</li>
 * </ul>
 *
 */
abstract class AbstractSignedInteger implements IntegerTypeDefinition, Serializable {
    private static final long serialVersionUID = 1L;

    private final QName name;
    private final SchemaPath path;
    private final String description;
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.2";
    private final String units;
    private final List<RangeConstraint> rangeStatements;

    /**
     * Construct SignedInteger
     *
     * @param name Name of type
     * @param description Description of type
     * @param minRange Minimal range
     * @param maxRange Maximum range
     * @param units Units
     */
    protected AbstractSignedInteger(final QName name, final String description, final Number minRange,
            final Number maxRange, final String units) {
        this.name = name;
        this.path = SchemaPath.create(true, name);
        this.description = description;
        this.units = units;
        final String rangeDescription = "Integer values between " + minRange + " and " + maxRange + ", inclusively.";
        this.rangeStatements = Collections.singletonList(BaseConstraints.newRangeConstraint(minRange, maxRange, Optional.of(rangeDescription),
                Optional.of("https://tools.ietf.org/html/rfc6020#section-9.2.4")));
    }

    @Override
    public IntegerTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return units;
    }

    @Override
    public QName getQName() {
        return name;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return description;
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
    public List<RangeConstraint> getRangeConstraints() {
        return rangeStatements;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(description);
        result = prime * result + Objects.hashCode(name);
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
        AbstractSignedInteger other = (AbstractSignedInteger) obj;
        if (!Objects.equals(description, other.description)) {
            return false;
        }
        if (!Objects.equals(name, other.name)) {
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
        builder.append("AbstractInteger [name=");
        builder.append(name);
        builder.append(", path=");
        builder.append(path);
        builder.append(", description=");
        builder.append(description);
        builder.append(", reference=");
        builder.append(REFERENCE);
        builder.append(", units=");
        builder.append(units);
        builder.append(", rangeStatements=");
        builder.append(rangeStatements);
        builder.append("]");
        return builder.toString();
    }
}
