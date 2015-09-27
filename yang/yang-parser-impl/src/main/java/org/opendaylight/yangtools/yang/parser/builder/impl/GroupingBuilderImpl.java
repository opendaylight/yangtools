/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractDocumentedDataNodeContainerBuilder;

public final class GroupingBuilderImpl extends AbstractDocumentedDataNodeContainerBuilder implements GroupingBuilder {
    private GroupingDefinitionImpl instance;
    // SchemaNode args
    private SchemaPath schemaPath;
    // DataSchemaNode args
    private boolean addedByUses;

    public GroupingBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
    }

    public GroupingBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path,
            final GroupingDefinition base) {
        super(moduleName, line, base.getQName(), Preconditions.checkNotNull(path, "Schema Path must not be null"), base);
        schemaPath = path;
        addedByUses = base.isAddedByUses();
        addedUnknownNodes.addAll(BuilderUtils.wrapUnknownNodes(moduleName, line, base.getUnknownSchemaNodes(), path,
                qname));
    }

    @Override
    public GroupingDefinition build() {
        if (instance != null) {
            return instance;
        }
        buildChildren();
        instance = new GroupingDefinitionImpl(qname, schemaPath, this);
        instance.addedByUses = addedByUses;

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    @Override
    public List<DataSchemaNodeBuilder> instantiateChildNodes(final Builder newParent) {
        final List<DataSchemaNodeBuilder> nodes = new ArrayList<>();
        for (DataSchemaNodeBuilder node : getChildNodeBuilders()) {
            DataSchemaNodeBuilder copy = CopyUtils.copy(node, newParent, true);
            BuilderUtils.setNodeAddedByUses(copy);
            nodes.add(copy);
        }
        return nodes;
    }

    @Override
    public Set<TypeDefinitionBuilder> instantiateTypedefs(final Builder newParent) {
        final Set<TypeDefinitionBuilder> nodes = new HashSet<>();
        for (TypeDefinitionBuilder node : getTypeDefinitionBuilders()) {
            TypeDefinitionBuilder copy = CopyUtils.copy(node, newParent, true);
            nodes.add(copy);
        }
        return nodes;
    }

    @Override
    public Set<GroupingBuilder> instantiateGroupings(final Builder newParent) {
        final Set<GroupingBuilder> nodes = new HashSet<>();
        for (GroupingBuilder node : getGroupingBuilders()) {
            GroupingBuilder copy = CopyUtils.copy(node, newParent, true);
            copy.setAddedByUses(true);
            for (DataSchemaNodeBuilder childNode : copy.getChildNodeBuilders()) {
                BuilderUtils.setNodeAddedByUses(childNode);
            }
            nodes.add(copy);
        }
        return nodes;
    }

    @Override
    public Set<UnknownSchemaNodeBuilder> instantiateUnknownNodes(final Builder newParent) {
        final Set<UnknownSchemaNodeBuilder> nodes = new HashSet<>();
        for (UnknownSchemaNodeBuilder node : addedUnknownNodes) {
            UnknownSchemaNodeBuilderImpl copy = CopyUtils.copy(node, newParent, true);
            copy.setAddedByUses(true);
            nodes.add(copy);
        }
        return nodes;
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
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        this.addedByUses = addedByUses;
    }

    @Override
    public String toString() {
        return "grouping " + qname.getLocalName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getParent());
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
        final GroupingBuilderImpl other = (GroupingBuilderImpl) obj;
        if (getParent() == null) {
            if (other.getParent() != null) {
                return false;
            }
        } else if (!getParent().equals(other.getParent())) {
            return false;
        }
        if (schemaPath == null) {
            if (other.schemaPath != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.schemaPath)) {
            return false;
        }
        return true;
    }

    @Override
    protected String getStatementName() {
        return "grouping";
    }

}
