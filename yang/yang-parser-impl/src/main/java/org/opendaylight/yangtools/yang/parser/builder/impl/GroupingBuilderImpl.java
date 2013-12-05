/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.YangNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractDataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.CopyUtils;
import org.opendaylight.yangtools.yang.parser.util.ParserUtils;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class GroupingBuilderImpl extends AbstractDataNodeContainerBuilder implements GroupingBuilder {
    private boolean isBuilt;
    private final GroupingDefinitionImpl instance;
    private SchemaPath schemaPath;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    private boolean addedByUses;

    public GroupingBuilderImpl(final String moduleName, final int line, final QName qname) {
        super(moduleName, line, qname);
        instance = new GroupingDefinitionImpl(qname);
    }

    @Override
    public GroupingDefinition build(YangNode parent) {
        if (!isBuilt) {
            instance.setParent(parent);
            instance.setPath(schemaPath);
            instance.setDescription(description);
            instance.setReference(reference);
            instance.setStatus(status);
            instance.setAddedByUses(addedByUses);

            // CHILD NODES
            for (DataSchemaNodeBuilder node : addedChildNodes) {
                DataSchemaNode child = node.build(instance);
                childNodes.put(child.getQName(), child);
            }
            instance.setChildNodes(childNodes);

            // GROUPINGS
            for (GroupingBuilder builder : addedGroupings) {
                groupings.add(builder.build(instance));
            }
            instance.setGroupings(groupings);

            // TYPEDEFS
            for (TypeDefinitionBuilder entry : addedTypedefs) {
                typedefs.add(entry.build(instance));
            }
            instance.setTypeDefinitions(typedefs);

            // USES
            for (UsesNodeBuilder builder : addedUsesNodes) {
                usesNodes.add(builder.build(instance));
            }
            instance.setUses(usesNodes);

            // UNKNOWN NODES
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build(instance));
            }
            Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            instance.setUnknownSchemaNodes(unknownNodes);

            isBuilt = true;
        }

        return instance;
    }

    @Override
    public Set<DataSchemaNodeBuilder> instantiateChildNodes(Builder newParent) {
        final Set<DataSchemaNodeBuilder> nodes = new HashSet<>();
        for (DataSchemaNodeBuilder node : addedChildNodes) {
            DataSchemaNodeBuilder copy = CopyUtils.copy(node, newParent, true);
            ParserUtils.setNodeAddedByUses(copy);
            nodes.add(copy);
        }
        return nodes;
    }

    @Override
    public Set<TypeDefinitionBuilder> instantiateTypedefs(Builder newParent) {
        final Set<TypeDefinitionBuilder> nodes = new HashSet<>();
        for (TypeDefinitionBuilder node : addedTypedefs) {
            TypeDefinitionBuilder copy = CopyUtils.copy(node, newParent, true);
            nodes.add(copy);
        }
        return nodes;
    }

    @Override
    public Set<GroupingBuilder> instantiateGroupings(Builder newParent) {
        final Set<GroupingBuilder> nodes = new HashSet<>();
        for (GroupingBuilder node : addedGroupings) {
            GroupingBuilder copy = CopyUtils.copy(node, newParent, true);
            copy.setAddedByUses(true);
            for (DataSchemaNodeBuilder childNode : copy.getChildNodeBuilders()) {
                ParserUtils.setNodeAddedByUses(childNode);
            }
            nodes.add(copy);
        }
        return nodes;
    }

    @Override
    public Set<UnknownSchemaNodeBuilder> instantiateUnknownNodes(Builder newParent) {
        final Set<UnknownSchemaNodeBuilder> nodes = new HashSet<>();
        for (UnknownSchemaNodeBuilder node : addedUnknownNodes) {
            UnknownSchemaNodeBuilder copy = CopyUtils.copy(node, newParent, true);
            copy.setAddedByUses(true);
            nodes.add(copy);
        }
        return nodes;
    }

    @Override
    public void setQName(QName qname) {
        this.qname = qname;
    }

    @Override
    public Set<TypeDefinitionBuilder> getTypeDefinitionBuilders() {
        return addedTypedefs;
    }

    @Override
    public void addTypedef(final TypeDefinitionBuilder type) {
        String typeName = type.getQName().getLocalName();
        for (TypeDefinitionBuilder addedTypedef : addedTypedefs) {
            throw new YangParseException(moduleName, type.getLine(), "Can not add typedef '" + typeName
                    + "': typedef with same name already declared at line " + addedTypedef.getLine());
        }
        addedTypedefs.add(type);
    }

    public void setTypedefs(final Set<TypeDefinition<?>> typedefs) {
        this.typedefs = typedefs;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public void setPath(SchemaPath schemaPath) {
        this.schemaPath = schemaPath;
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
        this.status = status;
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
        result = prime * result + ((parentBuilder == null) ? 0 : parentBuilder.hashCode());
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
        final GroupingBuilderImpl other = (GroupingBuilderImpl) obj;
        if (parentBuilder == null) {
            if (other.parentBuilder != null) {
                return false;
            }
        } else if (!parentBuilder.equals(other.parentBuilder)) {
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


    private final class GroupingDefinitionImpl implements GroupingDefinition {
        private final QName qname;
        private SchemaPath path;
        private YangNode parent;
        private String description;
        private String reference;
        private Status status;
        private boolean addedByUses;
        private Map<QName, DataSchemaNode> childNodes = Collections.emptyMap();
        private Set<GroupingDefinition> groupings = Collections.emptySet();
        private Set<TypeDefinition<?>> typeDefinitions = Collections.emptySet();
        private Set<UsesNode> uses = Collections.emptySet();
        private List<UnknownSchemaNode> unknownNodes = Collections.emptyList();

        private GroupingDefinitionImpl(final QName qname) {
            this.qname = qname;
        }

        @Override
        public QName getQName() {
            return qname;
        }

        @Override
        public SchemaPath getPath() {
            return path;
        }

        private void setPath(SchemaPath path) {
            this.path = path;
        }

        @Override
        public YangNode getParent() {
            return parent;
        }

        private void setParent(YangNode parent) {
            this.parent = parent;
        }

        @Override
        public String getDescription() {
            return description;
        }

        private void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        private void setReference(String reference) {
            this.reference = reference;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        private void setStatus(Status status) {
            this.status = status;
        }

        @Override
        public boolean isAddedByUses() {
            return addedByUses;
        }

        private void setAddedByUses(final boolean addedByUses) {
            this.addedByUses = addedByUses;
        }

        @Override
        public Set<DataSchemaNode> getChildNodes() {
            final Set<DataSchemaNode> result = new TreeSet<DataSchemaNode>(Comparators.SCHEMA_NODE_COMP);
            result.addAll(childNodes.values());
            return result;
        }

        private void setChildNodes(Map<QName, DataSchemaNode> childNodes) {
            this.childNodes = childNodes;
        }

        @Override
        public Set<GroupingDefinition> getGroupings() {
            return groupings;
        }

        private void setGroupings(Set<GroupingDefinition> groupings) {
            this.groupings = groupings;
        }

        @Override
        public Set<UsesNode> getUses() {
            return uses;
        }

        private void setUses(Set<UsesNode> uses) {
            this.uses = uses;
        }

        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return typeDefinitions;
        }

        private void setTypeDefinitions(Set<TypeDefinition<?>> typeDefinitions) {
            this.typeDefinitions = typeDefinitions;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        private void setUnknownSchemaNodes(List<UnknownSchemaNode> unknownNodes) {
            if (unknownNodes != null) {
                this.unknownNodes = unknownNodes;
            }
        }

        @Override
        public DataSchemaNode getDataChildByName(QName name) {
            return childNodes.get(name);
        }

        @Override
        public DataSchemaNode getDataChildByName(String name) {
            DataSchemaNode result = null;
            for (Map.Entry<QName, DataSchemaNode> entry : childNodes.entrySet()) {
                if (entry.getKey().getLocalName().equals(name)) {
                    result = entry.getValue();
                    break;
                }
            }
            return result;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((qname == null) ? 0 : qname.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
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
            final GroupingDefinitionImpl other = (GroupingDefinitionImpl) obj;
            if (qname == null) {
                if (other.qname != null) {
                    return false;
                }
            } else if (!qname.equals(other.qname)) {
                return false;
            }
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(GroupingDefinitionImpl.class.getSimpleName());
            sb.append("[");
            sb.append("qname=" + qname);
            sb.append("]");
            return sb.toString();
        }
    }

}
