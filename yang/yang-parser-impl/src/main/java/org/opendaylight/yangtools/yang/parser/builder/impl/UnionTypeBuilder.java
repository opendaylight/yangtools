/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractTypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Builder for YANG union type. User can add type to this union as
 * TypeDefinition object (resolved type) or in form of TypeDefinitionBuilder.
 * When build is called, types in builder form will be built and add to resolved
 * types.
 *
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
public final class UnionTypeBuilder extends AbstractTypeAwareBuilder implements TypeDefinitionBuilder {
    private static final SchemaPath SCHEMA_PATH = SchemaPath.create(true, BaseTypes.UNION_QNAME);
    private static final String NAME = "union";

    private final List<TypeDefinition<?>> types;
    private final List<TypeDefinitionBuilder> typedefs;
    private UnionType instance;
    private boolean isBuilt;

    private final List<QName> baseTypesQNames = new ArrayList<>();

    public UnionTypeBuilder(final String moduleName, final int line) {
        super(moduleName, line, BaseTypes.UNION_QNAME);
        types = new ArrayList<>();
        typedefs = new ArrayList<>();
    }

    public List<QName> getBaseTypeQNames() {
        return baseTypesQNames;
    }

    @Override
    public void setTypeQName(final QName qname) {
        baseTypesQNames.add(qname);
    }

    public List<TypeDefinition<?>> getTypes() {
        return types;
    }

    @Override
    public void setQName(final QName qname) {
        throw new UnsupportedOperationException("Can not set qname to union type");
    }

    @Override
    public TypeDefinition<?> getType() {
        return null;
    }

    public List<TypeDefinitionBuilder> getTypedefs() {
        return typedefs;
    }

    @Override
    public TypeDefinitionBuilder getTypedef() {
        return null;
    }

    @Override
    public void setType(final TypeDefinition<?> type) {
        types.add(type);
    }

    @Override
    public void setTypedef(final TypeDefinitionBuilder tdb) {
        typedefs.add(tdb);
    }

    @Override
    public UnionType build() {
        if (!isBuilt) {
            for (TypeDefinitionBuilder tdb : typedefs) {
                types.add(tdb.build());
            }
            instance = UnionType.create(types);
            isBuilt = true;
        }
        return instance;
    }

    @Override
    public void setDescription(final String description) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set description to " + NAME);
    }

    @Override
    public void setReference(final String reference) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set reference to " + NAME);
    }

    @Override
    public void setStatus(final Status status) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set status to " + NAME);
    }

    @Override
    public boolean isAddedByUses() {
        return false;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        throw new YangParseException(getModuleName(), getLine(), "Union type can not be added by uses.");
    }

    @Override
    public void addUnknownNodeBuilder(final UnknownSchemaNodeBuilder unknownNode) {
        // not yet supported
    }

    @Override
    public SchemaPath getPath() {
        return SCHEMA_PATH;
    }

    @Override
    public void setPath(final SchemaPath path) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set path to " + NAME);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getReference() {
        return null;
    }

    @Override
    public Status getStatus() {
        return null;
    }

    @Override
    public List<RangeConstraint> getRanges() {
        return Collections.emptyList();
    }

    @Override
    public void setRanges(final List<RangeConstraint> ranges) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set ranges to " + NAME);
    }

    @Override
    public List<LengthConstraint> getLengths() {
        return Collections.emptyList();
    }

    @Override
    public void setLengths(final List<LengthConstraint> lengths) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set lengths to " + NAME);
    }

    @Override
    public List<PatternConstraint> getPatterns() {
        return Collections.emptyList();
    }

    @Override
    public void setPatterns(final List<PatternConstraint> patterns) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set patterns to " + NAME);
    }

    @Override
    public Integer getFractionDigits() {
        return null;
    }

    @Override
    public void setFractionDigits(final Integer fractionDigits) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set fraction digits to " + NAME);
    }

    @Override
    public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
        return Collections.emptyList();
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public void setDefaultValue(final Object defaultValue) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set default value to " + NAME);
    }

    @Override
    public String getUnits() {
        return null;
    }

    @Override
    public void setUnits(final String units) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set units to " + NAME);
    }

    @Override
    public String toString() {
        return UnionTypeBuilder.class.getSimpleName() + "[" +
                "types=" +
                types +
                ", typedefs=" +
                typedefs +
                "]";
    }

}
