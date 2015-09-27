/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractTypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class TypeDefinitionBuilderImpl extends AbstractTypeAwareBuilder implements TypeDefinitionBuilder {
    private SchemaPath schemaPath;
    private List<RangeConstraint> ranges = Collections.emptyList();
    private List<LengthConstraint> lengths = Collections.emptyList();
    private List<PatternConstraint> patterns = Collections.emptyList();
    private Integer fractionDigits = null;

    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    private String units;
    private Object defaultValue;
    private boolean addedByUses;

    public TypeDefinitionBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
    }

    public TypeDefinitionBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path, final ExtendedType base) {
        super(moduleName, line, base.getQName());
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");

        this.type = base.getBaseType();
        this.description = base.getDescription();
        this.reference = base.getReference();
        this.status = base.getStatus();
        this.units = base.getUnits();
        this.defaultValue = base.getDefaultValue();

        this.addedByUses = base.isAddedByUses();
        this.ranges = base.getRangeConstraints();
        this.lengths = base.getLengthConstraints();
        this.patterns = base.getPatternConstraints();
        this.fractionDigits = base.getFractionDigits();
        this.unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public TypeDefinition<? extends TypeDefinition<?>> build() {
        TypeDefinition<?> result;
        ExtendedType.Builder typeBuilder;
        if (type == null) {
            if (typedef == null) {
                throw new YangParseException("Unresolved type: '" + qname.getLocalName() + "'.");
            } else {
                type = typedef.build();
            }
        }

        typeBuilder = ExtendedType.builder(qname, type, Optional.fromNullable(description),
                Optional.fromNullable(reference), schemaPath);
        typeBuilder.status(status);
        typeBuilder.units(units);
        typeBuilder.defaultValue(defaultValue);
        typeBuilder.addedByUses(addedByUses);
        typeBuilder.ranges(ranges);
        typeBuilder.lengths(lengths);
        typeBuilder.patterns(patterns);
        typeBuilder.fractionDigits(fractionDigits);

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
        typeBuilder.unknownSchemaNodes(unknownNodes);
        result = typeBuilder.build();
        return result;
    }

    @Override
    public void setQName(final QName qname) {
        this.qname = qname;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public void setPath(final SchemaPath path) {
        this.schemaPath = path;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(final Status status) {
        if (status != null) {
            this.status = status;
        }
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        this.addedByUses = addedByUses;
    }

    @Override
    public String getUnits() {
        return units;
    }

    @Override
    public void setUnits(final String units) {
        this.units = units;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(final Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public List<RangeConstraint> getRanges() {
        return ranges;
    }

    @Override
    public void setRanges(final List<RangeConstraint> ranges) {
        if (ranges != null) {
            this.ranges = ranges;
        }
    }

    @Override
    public List<LengthConstraint> getLengths() {
        return lengths;
    }

    @Override
    public void setLengths(final List<LengthConstraint> lengths) {
        if (lengths != null) {
            this.lengths = lengths;
        }
    }

    @Override
    public List<PatternConstraint> getPatterns() {
        return patterns;
    }

    @Override
    public void setPatterns(final List<PatternConstraint> patterns) {
        if (patterns != null) {
            this.patterns = patterns;
        }
    }

    @Override
    public Integer getFractionDigits() {
        return fractionDigits;
    }

    @Override
    public void setFractionDigits(final Integer fractionDigits) {
        this.fractionDigits = fractionDigits;
    }

    @Override
    public String toString() {
        return "typedef " + qname.getLocalName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(schemaPath);

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
        if (!super.equals(obj)) {
            return false;
        }
        TypeDefinitionBuilderImpl other = (TypeDefinitionBuilderImpl) obj;

        if (schemaPath == null) {
            if (other.schemaPath != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.schemaPath)) {
            return false;
        }

        return true;
    }

}
