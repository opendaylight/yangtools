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
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

/**
 * Extended Type represents YANG type derived from other type.
 *
 * Extended type object is decorator on top of existing {@link TypeDefinition}
 * which represents original type, and extended type
 * may define additional constraints, modify description or reference
 * of parent type or provide new type capture for specific use-cases.
 *
 * @deprecated Use of this class is deprecated, use {@link DerivedType} instead.
 */
@Deprecated
public class ExtendedType implements TypeDefinition<TypeDefinition<?>>, Serializable {
    private static final long serialVersionUID = 1L;

    private final QName typeName;
    private final TypeDefinition<?> baseType;
    private final SchemaPath path;
    private final String description;
    private final String reference;
    private final List<UnknownSchemaNode> unknownSchemaNodes;

    private final Status status;
    private final String units;
    private final Object defaultValue;
    private final boolean addedByUses;

    private List<RangeConstraint> ranges = Collections.emptyList();
    private List<LengthConstraint> lengths = Collections.emptyList();
    private List<PatternConstraint> patterns = Collections.emptyList();
    private Integer fractionDigits = null;

    /**
     *
     * Creates Builder for extended / derived type.
     *
     * @param typeName QName of derived type
     * @param baseType Base type of derived type
     * @param description Description of type
     * @param reference Reference of Type
     * @param path Schema path to type definition.
     */
    public static final Builder builder(final QName typeName, final TypeDefinition<?> baseType,
            final Optional<String> description, final Optional<String> reference, final SchemaPath path) {
        return new Builder(typeName, baseType, description.or(""), reference.or(""), path);
    }

    public static class Builder {
        private final QName typeName;
        private final TypeDefinition<?> baseType;

        private final SchemaPath path;
        private final String description;
        private final String reference;

        private List<UnknownSchemaNode> unknownSchemaNodes = Collections
                .emptyList();
        private Status status = Status.CURRENT;
        private String units = null;
        private Object defaultValue = null;
        private boolean addedByUses;

        private List<RangeConstraint> ranges = Collections.emptyList();
        private List<LengthConstraint> lengths = Collections.emptyList();
        private List<PatternConstraint> patterns = Collections.emptyList();
        private Integer fractionDigits = null;

        /**
         * Creates Builder for extended / derived type.
         *
         * @param typeName QName of derived type
         * @param baseType Base type of derived type
         * @param description Description of type
         * @param reference Reference of Type
         * @param path Schema path to type definition.
         */
        protected Builder(final QName typeName, final TypeDefinition<?> baseType,
                final String description, final String reference,
                final SchemaPath path) {
            this.typeName = Preconditions.checkNotNull(typeName, "type name must not be null.");
            this.baseType = Preconditions.checkNotNull(baseType, "base type must not be null");
            this.path = Preconditions.checkNotNull(path, "path must not be null.");
            this.description = description;
            this.reference = reference;
        }

        public Builder status(final Status status) {
            this.status = status;
            return this;
        }

        public Builder units(final String units) {
            this.units = units;
            return this;
        }

        public Builder defaultValue(final Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder addedByUses(final boolean addedByUses) {
            this.addedByUses = addedByUses;
            return this;
        }

        public Builder unknownSchemaNodes(
                final List<UnknownSchemaNode> unknownSchemaNodes) {
            if (unknownSchemaNodes.isEmpty()) {
                this.unknownSchemaNodes = Collections.emptyList();
            } else {
                this.unknownSchemaNodes = unknownSchemaNodes;
            }
            return this;
        }

        public Builder ranges(final List<RangeConstraint> ranges) {
            if (ranges != null) {
                this.ranges = ranges;
            }
            return this;
        }

        public Builder lengths(final List<LengthConstraint> lengths) {
            if (lengths != null) {
                this.lengths = lengths;
            }
            return this;
        }

        public Builder patterns(final List<PatternConstraint> patterns) {
            if (patterns != null) {
                this.patterns = patterns;
            }
            return this;
        }

        public Builder fractionDigits(final Integer fractionDigits) {
            this.fractionDigits = fractionDigits;
            return this;
        }

        public ExtendedType build() {
            return new ExtendedType(this);
        }
    }

    private ExtendedType(final Builder builder) {
        this.typeName = builder.typeName;
        this.baseType = builder.baseType;
        this.path = builder.path;
        this.description = builder.description;
        this.reference = builder.reference;
        this.unknownSchemaNodes = builder.unknownSchemaNodes;
        this.status = builder.status;
        this.units = builder.units;
        this.defaultValue = builder.defaultValue;
        this.addedByUses = builder.addedByUses;

        this.ranges = builder.ranges;
        this.lengths = builder.lengths;
        this.patterns = builder.patterns;
        this.fractionDigits = builder.fractionDigits;
    }

    @Override
    public TypeDefinition<?> getBaseType() {
        return baseType;
    }

    @Override
    public String getUnits() {
        return units;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public QName getQName() {
        return typeName;
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
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownSchemaNodes;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExtendedType)) {
            return false;
        }

        ExtendedType that = (ExtendedType) o;
        if (path != null ? !path.equals(that.path) : that.path != null) {
            return false;
        }
        if (typeName != null ? !typeName.equals(that.typeName) : that.typeName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = typeName != null ? typeName.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ExtendedType [typeName=");
        builder.append(typeName);
        builder.append(", baseType=");
        builder.append(baseType);
        builder.append(", path=");
        builder.append(path);
        builder.append(", description=");
        builder.append(description);
        builder.append(", reference=");
        builder.append(reference);
        builder.append(", unknownSchemaNodes=");
        builder.append(unknownSchemaNodes);
        builder.append(", status=");
        builder.append(status);
        builder.append(", units=");
        builder.append(units);
        builder.append(", defaultValue=");
        builder.append(defaultValue);
        builder.append("]");
        return builder.toString();
    }

    public List<RangeConstraint> getRangeConstraints() {
        return ranges;
    }

    public List<LengthConstraint> getLengthConstraints() {
        return lengths;
    }

    public List<PatternConstraint> getPatternConstraints() {
        return patterns;
    }

    public Integer getFractionDigits() {
        return fractionDigits;
    }
}
