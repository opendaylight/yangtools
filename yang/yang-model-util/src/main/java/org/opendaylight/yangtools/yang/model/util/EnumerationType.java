/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * The <code>default</code> implementation of Enumeration Type Definition
 * interface.
 *
 * @see EnumTypeDefinition
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#enumerationTypeBuilder(SchemaPath)} instead
 */
@Deprecated
public final class EnumerationType implements EnumTypeDefinition {
    private static final String DESCRIPTION = "The enumeration built-in type represents values from a set of assigned names.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.6";
    private static final String UNITS = "";

    private final SchemaPath path;
    private final EnumPair defaultEnum;
    private final List<EnumPair> enums;

    private EnumerationType(final SchemaPath path, final List<EnumPair> enums, final Optional<EnumPair> defaultEnum) {
        this.path = Preconditions.checkNotNull(path,"path must not be null");
        this.enums = ImmutableList.copyOf(Preconditions.checkNotNull(enums, "enums must not be null."));
        if(defaultEnum.isPresent()) {
            Preconditions.checkArgument(enums.contains(defaultEnum.get()),"defaultEnum must be contained in defined enumerations.");
            this.defaultEnum = defaultEnum.get();
        } else {
            this.defaultEnum = null;
        }
    }

    /**
     * Constructs a new enumeration
     *
     * @param path Schema Path to definition point of this enumeration
     * @param enums List of defined enumeration values
     * @param defaultValue {@link Optional#of(Object)} of default value, {@link Optional#absent()} if no default value is defined.
     *        If defaultValue is set, it must be present in provided list of enumerations.
     */
    public static EnumerationType create(final SchemaPath path, final List<EnumPair> enums, final Optional<EnumPair> defaultValue) {
        return new EnumerationType(path, enums, defaultValue);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getBaseType()
     */
    @Override
    public EnumTypeDefinition getBaseType() {
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
     * org.opendaylight.yangtools.yang.model.api.TypeDefinition#getDefaultValue()
     */
    @Override
    public Object getDefaultValue() {
        return defaultEnum;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.opendaylight.yangtools.yang.model.api.SchemaNode#getQName()
     */
    @Override
    public QName getQName() {
        return BaseTypes.ENUMERATION_QNAME;
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

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opendaylight.yangtools.yang.model.base.type.api.EnumTypeDefinition#getValues()
     */
    @Override
    public List<EnumPair> getValues() {
        return enums;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(defaultEnum);
        result = prime * result + Objects.hashCode(enums);
        result = prime * result + BaseTypes.ENUMERATION_QNAME.hashCode();
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
        EnumerationType other = (EnumerationType) obj;
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
        builder.append("EnumerationType [name=");
        builder.append(BaseTypes.ENUMERATION_QNAME);
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
}
