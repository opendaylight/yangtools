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
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#bitsTypeBuilder(SchemaPath)} instead
 */
@Deprecated
public final class BitsType implements BitsTypeDefinition {
    private static final QName NAME = BaseTypes.BITS_QNAME;
    private static final String DESCRIPTION = "The bits built-in type represents a bit set. "
            + "That is, a bits value is a set of flags identified by small integer position "
            + "numbers starting at 0.  Each bit number has an assigned name.";

    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.7";
    private static final String UNITS = "";
    private final SchemaPath path;
    private final List<Bit> bits;

    /**
     * Constructor with explicit definition of bits assigned to BitsType.
     *
     * @param path
     * @param bits
     */
    private BitsType(final SchemaPath path, final List<Bit> bits) {
        super();
        this.bits = ImmutableList.copyOf(bits);
        this.path = Preconditions.checkNotNull(path, "path must not be null");
    }

    public static BitsType create(final SchemaPath path, final List<Bit> bits) {
        return new BitsType(path,bits);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getBaseType()
     */
    @Override
    public BitsTypeDefinition getBaseType() {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.TypeDefinition#getUnits()
     */
    @Override
    public String getUnits() {
        return UNITS;
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
        return bits;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getQName()
     */
    @Override
    public QName getQName() {
        return NAME;
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
        return DESCRIPTION;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getReference()
     */
    @Override
    public String getReference() {
        return REFERENCE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getStatus()
     */
    @Override
    public Status getStatus() {
        return Status.CURRENT;
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
        builder.append(NAME);
        builder.append(", path=");
        builder.append(path);
        builder.append(", description=");
        builder.append(DESCRIPTION);
        builder.append(", reference=");
        builder.append(REFERENCE);
        builder.append(", bits=");
        builder.append(bits);
        builder.append(", units=");
        builder.append(UNITS);
        builder.append("]");
        return builder.toString();
    }
}
