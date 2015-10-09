/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * The <code>default</code> implementation of Bits Type Definition interface.
 *
 * @see BitsTypeDefinition
 */
public final class BitsType implements BitsTypeDefinition {
    private static final QName NAME = BaseTypes.BITS_QNAME;
    private static final String DEFAULT_DESCRIPTION = "The bits built-in type represents a bit set. "
            + "That is, a bits value is a set of flags identified by small integer position "
            + "numbers starting at 0.  Each bit number has an assigned name.";

    private static final String DEFAULT_REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.7";
    private static final String DEFAULT_UNITS = "";
    private final SchemaPath path;
    private final List<Bit> bits;
    private final String description;
    private final String reference;
    private final Status status;
    private final String units;
    private final Object defaultValue;
    private final BitsTypeDefinition baseType;

    BitsType(final SchemaPath path, final Status status, final String description, final String reference,
            final List<UnknownSchemaNode> unknownSchemaNodes, final BitsTypeDefinition baseType, final String units,
            final Object defaultValue, final List<Bit> bits) {
        this.path = Preconditions.checkNotNull(path, "path must not be null");
        this.bits = ImmutableList.copyOf(bits);

        this.description = description;
        this.reference = reference;
        this.status = Preconditions.checkNotNull(status);
        this.units = units;
        this.defaultValue = defaultValue;
        this.baseType = baseType;
    }

    /**
     * @deprecated Use {@link BitsTypeBuilder} instead. This method will be removed in Boron release cycle.
     */
    @Deprecated
    public static BitsType create(final SchemaPath path, final List<Bit> bits) {
        return new BitsType(path, Status.CURRENT, DEFAULT_DESCRIPTION, DEFAULT_REFERENCE, null, null, DEFAULT_UNITS,
            bits, bits);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getBaseType()
     */
    @Override
    public BitsTypeDefinition getBaseType() {
        return baseType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.TypeDefinition#getUnits()
     */
    @Override
    public String getUnits() {
        return units;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getDefaultValue
     * ()
     */
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getQName()
     */
    @Override
    public QName getQName() {
        return getPath().getLastComponent();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getPath()
     */
    @Override
    public SchemaPath getPath() {
        return path;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.SchemaNode#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getReference()
     */
    @Override
    public String getReference() {
        return reference;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getStatus()
     */
    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public List<Bit> getBits() {
        return bits;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(bits);
        result = prime * result + NAME.hashCode();
        result = prime * result + path.hashCode();
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
        BitsType other = (BitsType) obj;
        return Objects.equals(bits, other.bits) && Objects.equals(path, other.path);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BitsType [name=");
        builder.append(getQName());
        builder.append(", path=");
        builder.append(path);
        builder.append(", description=");
        builder.append(description);
        builder.append(", reference=");
        builder.append(reference);
        builder.append(", bits=");
        builder.append(bits);
        builder.append(", units=");
        builder.append(getUnits());
        builder.append("]");
        return builder.toString();
    }
}
