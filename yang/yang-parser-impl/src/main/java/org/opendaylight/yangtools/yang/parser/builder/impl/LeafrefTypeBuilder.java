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
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.Leafref;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractTypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public class LeafrefTypeBuilder extends AbstractTypeAwareBuilder implements TypeDefinitionBuilder {

    private static final String NAME = "leafref";

    private final RevisionAwareXPath targetPath;
    private DataSchemaNode targetNode;

    public LeafrefTypeBuilder(final String moduleName, final int line, final RevisionAwareXPath targetPath) {
        super(moduleName, line, BaseTypes.constructQName(NAME));
        this.targetPath = targetPath;
    }

    @Override
    public void setQName(QName qname) {
        this.qname = qname;
    }

    @Override
    public Leafref build() {
        return new Leafref(targetPath, targetNode);
    }

    public RevisionAwareXPath getTargetPath() {
        return targetPath;
    }

    public DataSchemaNode getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(DataSchemaNode targetNode) {
        this.targetNode = targetNode;
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
        throw new YangParseException(moduleName, line, "Can not set type to " + NAME);
    }

    @Override
    public void setTypedef(final TypeDefinitionBuilder tdb) {
        throw new YangParseException(moduleName, line, "Can not set type to " + NAME);
    }

    @Override
    public void setDescription(final String description) {
        throw new YangParseException(moduleName, line, "Can not set description to " + NAME);
    }

    @Override
    public void setReference(final String reference) {
        throw new YangParseException(moduleName, line, "Can not set reference to " + NAME);
    }

    @Override
    public void setStatus(final Status status) {
        throw new YangParseException(moduleName, line, "Can not set status to " + NAME);
    }

    @Override
    public boolean isAddedByUses() {
        return false;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        throw new YangParseException(moduleName, line, "Identityref type can not be added by uses.");
    }

    @Override
    public void addUnknownNodeBuilder(final UnknownSchemaNodeBuilder unknownNode) {
        throw new YangParseException(moduleName, line, "Can not add unknown node to " + NAME);
    }

    @Override
    public QName getQName() {
        return null;
    }

    @Override
    public SchemaPath getPath() {
        return null;
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
    public void setRanges(List<RangeConstraint> ranges) {
        throw new YangParseException(moduleName, line, "Can not set ranges to " + NAME);
    }

    @Override
    public List<LengthConstraint> getLengths() {
        return Collections.emptyList();
    }

    @Override
    public void setLengths(List<LengthConstraint> lengths) {
        throw new YangParseException(moduleName, line, "Can not set lengths to " + NAME);
    }

    @Override
    public List<PatternConstraint> getPatterns() {
        return Collections.emptyList();
    }

    @Override
    public void setPatterns(List<PatternConstraint> patterns) {
        throw new YangParseException(moduleName, line, "Can not set patterns to " + NAME);
    }

    @Override
    public Integer getFractionDigits() {
        return null;
    }

    @Override
    public void setFractionDigits(Integer fractionDigits) {
        throw new YangParseException(moduleName, line, "Can not set fraction digits to " + NAME);
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
    public void setDefaultValue(Object defaultValue) {
        throw new YangParseException(moduleName, line, "Can not set default value to " + NAME);
    }

    @Override
    public String getUnits() {
        return null;
    }

    @Override
    public void setUnits(String units) {
        throw new YangParseException(moduleName, line, "Can not set units to " + NAME);
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(LeafrefTypeBuilder.class.getSimpleName());
        result.append("[");
        result.append("targetPath=");
        result.append(targetPath);
        result.append("]");
        return result.toString();
    }

}
