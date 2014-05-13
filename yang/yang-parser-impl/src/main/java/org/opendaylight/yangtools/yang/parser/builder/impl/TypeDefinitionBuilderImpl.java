/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.model.api.type.*;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.UnknownType;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractTypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
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
        this.schemaPath = path;
    }

    public TypeDefinitionBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path, final ExtendedType base) {
        super(moduleName, line, base.getQName());
        this.schemaPath = path;

        this.type = base.getBaseType();
        this.description = base.getDescription();
        this.reference = base.getReference();
        this.status = base.getStatus();
        this.units = base.getUnits();
        this.defaultValue = base.getDefaultValue();

        ExtendedType ext = base;
        this.addedByUses = ext.isAddedByUses();
        this.ranges = ext.getRangeConstraints();
        this.lengths = ext.getLengthConstraints();
        this.patterns = ext.getPatternConstraints();
        this.fractionDigits = ext.getFractionDigits();
        this.unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public TypeDefinition<? extends TypeDefinition<?>> build() {
        TypeDefinition<?> result = null;
        ExtendedType.Builder typeBuilder = null;
        if ((type == null || type instanceof UnknownType) && typedef == null) {
            throw new YangParseException("Unresolved type: '" + qname.getLocalName() + "'.");
        }
        if (type == null || type instanceof UnknownType) {
            type = typedef.build();
        }

        typeBuilder = new ExtendedType.Builder(qname, type, description, reference, schemaPath);
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
    public void setQName(QName qname) {
        this.qname = qname;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public void setPath(SchemaPath path) {
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
    public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
        return Collections.emptyList();
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
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
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
