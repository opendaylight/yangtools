/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

/**
 * The Abstract Integer class defines implementation of IntegerTypeDefinition
 * interface which represents UNSIGNED Integer values defined in Yang language. <br>
 * The integer built-in types in Yang are uint8, uint16, uint32, and uint64.
 * They represent unsigned integers of different sizes:
 *
 * <ul>
 * <li>uint8 - represents integer values between 0 and 255, inclusively.</li>
 * <li>uint16 - represents integer values between 0 and 65535, inclusively.</li>
 * <li>uint32 - represents integer values between 0 and 4294967295, inclusively.
 * </li>
 * <li>uint64 - represents integer values between 0 and 18446744073709551615,
 * inclusively.</li>
 * </ul>
 *
 */
abstract class AbstractUnsignedInteger implements UnsignedIntegerTypeDefinition {
    private static final long MIN_VALUE = 0;
    private final QName name;
    private final SchemaPath path;
    private final String description;
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.2";
    private final String units;
    private final List<RangeConstraint> rangeStatements;

    /**
     *
     * @param name
     * @param description
     * @param maxRange
     * @param units
     */
    public AbstractUnsignedInteger(final QName name, final String description, final Number maxRange, final String units) {
        this.name = name;
        this.path = SchemaPath.create(Collections.singletonList(name), true);
        this.description = description;
        this.units = units;
        this.rangeStatements = new ArrayList<RangeConstraint>();
        final String rangeDescription = "Integer values between " + MIN_VALUE + " and " + maxRange + ", inclusively.";
        this.rangeStatements.add(BaseConstraints.rangeConstraint(MIN_VALUE, maxRange, rangeDescription,
                "https://tools.ietf.org/html/rfc6020#section-9.2.4"));
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
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((rangeStatements == null) ? 0 : rangeStatements.hashCode());
        result = prime * result + ((units == null) ? 0 : units.hashCode());
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
        AbstractUnsignedInteger other = (AbstractUnsignedInteger) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (rangeStatements == null) {
            if (other.rangeStatements != null) {
                return false;
            }
        } else if (!rangeStatements.equals(other.rangeStatements)) {
            return false;
        }
        if (units == null) {
            if (other.units != null) {
                return false;
            }
        } else if (!units.equals(other.units)) {
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
