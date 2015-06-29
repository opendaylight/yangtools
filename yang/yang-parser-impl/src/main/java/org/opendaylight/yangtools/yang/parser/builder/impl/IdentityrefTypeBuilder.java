/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Preconditions;
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
import org.opendaylight.yangtools.yang.model.util.IdentityrefType;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractTypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Builder for YANG identityref type.
 */
public final class IdentityrefTypeBuilder extends AbstractTypeAwareBuilder implements TypeDefinitionBuilder {
    private static final String NAME = "identityref";

    private final String baseString;
    private SchemaPath schemaPath;
    private IdentitySchemaNodeBuilder baseIdentity;

    public IdentityrefTypeBuilder(final String moduleName, final int line, final String baseString,
            final SchemaPath schemaPath) {
        super(moduleName, line, BaseTypes.constructQName(NAME));
        this.baseString = baseString;
        this.schemaPath = Preconditions.checkNotNull(schemaPath, "Schema Path must not be null");

    }

    @Override
    public void setQName(final QName qname) {
        this.qname = qname;
    }

    @Override
    public IdentityrefType build() {
        return IdentityrefType.create(schemaPath, baseIdentity.build());
    }

    public String getBaseString() {
        return baseString;
    }

    public void setBaseIdentity(final IdentitySchemaNodeBuilder baseIdentity) {
        this.baseIdentity = baseIdentity;
    }

    @Override
    public TypeDefinition<?> getType() {
        return null;
    }

    @Override
    public TypeDefinitionBuilder getTypedef() {
        return null;
    }

    @Override
    public void setType(final TypeDefinition<?> type) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set type to " + NAME);
    }

    @Override
    public void setTypedef(final TypeDefinitionBuilder tdb) {
        throw new YangParseException(getModuleName(), getLine(), "Can not set type to " + NAME);
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
        throw new YangParseException(getModuleName(), getLine(), "Identityref type can not be added by uses.");
    }

    @Override
    public void addUnknownNodeBuilder(final UnknownSchemaNodeBuilder unknownNode) {
        throw new YangParseException(getModuleName(), getLine(), "Can not add unknown node to " + NAME);
    }

    @Override
    public QName getQName() {
        return qname;
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
        final StringBuilder result = new StringBuilder(IdentityrefTypeBuilder.class.getSimpleName());
        result.append("[");
        result.append(", base=");
        result.append(baseIdentity);
        result.append("]");
        return result.toString();
    }

}
