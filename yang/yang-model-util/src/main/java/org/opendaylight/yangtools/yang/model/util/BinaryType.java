/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;

/**
 * The <code>default</code> implementation of Binary Type Definition interface.
 *
 * @see BinaryTypeDefinition
 */
public final class BinaryType implements BinaryTypeDefinition {
    private static final String DESCRIPTION = "The binary built-in type represents any binary data, i.e., a sequence of octets.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.8";
    private static final String UNITS = "";

    private static final QName QNAME = BaseTypes.BINARY_QNAME;

    private static final BinaryType INSTANCE = new BinaryType();

    private static final SchemaPath PATH = SchemaPath.create(Collections.singletonList(QNAME), true);
    private final List<Byte> bytes = Collections.emptyList();
    private final List<LengthConstraint> lengthConstraints;

    private BinaryType() {
        this.lengthConstraints = Collections.singletonList(
                BaseConstraints.newLengthConstraint(0, Long.MAX_VALUE, Optional.of(""), Optional.of("")));
    }

    public static BinaryType getInstance() {
        return INSTANCE;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getBaseType()
     */
    @Override
    public BinaryTypeDefinition getBaseType() {
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
        return bytes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getQName()
     */
    @Override
    public QName getQName() {
        return QNAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getPath()
     */
    @Override
    public SchemaPath getPath() {
        return PATH;
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.base.type.api.BinaryTypeDefinition
     * #getLengthConstraint ()
     */
    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return lengthConstraints;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bytes == null) ? 0 : bytes.hashCode());
        result = prime * result + ((lengthConstraints == null) ? 0 : lengthConstraints.hashCode());
        result = prime * result + QNAME.hashCode();
        result = prime * result + PATH.hashCode();
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
        BinaryType other = (BinaryType) obj;
        if (bytes == null) {
            if (other.bytes != null) {
                return false;
            }
        } else if (!bytes.equals(other.bytes)) {
            return false;
        }
        if (lengthConstraints == null) {
            if (other.lengthConstraints != null) {
                return false;
            }
        } else if (!lengthConstraints.equals(other.lengthConstraints)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BinaryType [name=");
        builder.append(QNAME);
        builder.append(", description=");
        builder.append(DESCRIPTION);
        builder.append(", reference=");
        builder.append(REFERENCE);
        builder.append(", bytes=");
        builder.append(bytes);
        builder.append(", lengthConstraints=");
        builder.append(lengthConstraints);
        builder.append(", units=");
        builder.append(UNITS);
        builder.append("]");
        return builder.toString();
    }
}
