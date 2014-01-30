/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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

    public GroupingBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        schemaPath = path;
        instance = new GroupingDefinitionImpl(qname, path);
    }

    public GroupingBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path, final GroupingDefinition base) {
        super(moduleName, line, base.getQName());
        schemaPath = path;
        instance = new GroupingDefinitionImpl(qname, path);

        instance.description = base.getDescription();
        instance.reference = base.getReference();
        instance.status = base.getStatus();
        instance.addedByUses = base.isAddedByUses();

        URI ns = qname.getNamespace();
        Date rev = qname.getRevision();
        String pref = qname.getPrefix();
        addedChildNodes.addAll(ParserUtils.wrapChildNodes(moduleName, line, base.getChildNodes(), path, ns, rev, pref));
        addedGroupings.addAll(ParserUtils.wrapGroupings(moduleName, line, base.getGroupings(), path, ns, rev, pref));
        addedTypedefs.addAll(ParserUtils.wrapTypedefs(moduleName, line, base, path, ns, rev, pref));
        addedUnknownNodes.addAll(ParserUtils.wrapUnknownNodes(moduleName, line, base.getUnknownSchemaNodes(), path, ns,
                rev, pref));

        instance.uses.addAll(base.getUses());
    }

    @Override
    public GroupingDefinition build() {
        if (!isBuilt) {
            // CHILD NODES
            for (DataSchemaNodeBuilder node : addedChildNodes) {
                childNodes.add(node.build());
            }
            instance.addChildNodes(childNodes);

            // GROUPINGS
            for (GroupingBuilder builder : addedGroupings) {
                groupings.add(builder.build());
            }
            instance.addGroupings(groupings);

            // TYPEDEFS
            for (TypeDefinitionBuilder entry : addedTypedefs) {
                typedefs.add(entry.build());
            }
            instance.addTypeDefinitions(typedefs);

            // USES
            for (UsesNodeBuilder builder : addedUsesNodes) {
                usesNodes.add(builder.build());
            }
            instance.addUses(usesNodes);

            // UNKNOWN NODES
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build());
            }
            Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            instance.addUnknownSchemaNodes(unknownNodes);

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
            if (newParent instanceof DataSchemaNodeBuilder) {
                ParserUtils.setNodeConfig(copy, ((DataSchemaNodeBuilder)newParent).isConfiguration());
            }
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

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public String getDescription() {
        return instance.description;
    }

    @Override
    public void setDescription(final String description) {
        instance.description = description;
    }

    @Override
    public String getReference() {
        return instance.reference;
    }

    @Override
    public void setReference(final String reference) {
        instance.reference = reference;
    }

    @Override
    public Status getStatus() {
        return instance.status;
    }

    @Override
    public void setStatus(Status status) {
        if (status != null) {
            instance.status = status;
        }
    }

    @Override
    public boolean isAddedByUses() {
        return instance.addedByUses;
    }

    @Override
    public void setAddedByUses(final boolean addedByUses) {
        instance.addedByUses = addedByUses;
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


    private static final class GroupingDefinitionImpl implements GroupingDefinition {
        private final QName qname;
        private final SchemaPath path;
        private String description;
        private String reference;
        private Status status;
        private boolean addedByUses;
        private final Set<DataSchemaNode> childNodes = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<GroupingDefinition> groupings = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<TypeDefinition<?>> typeDefinitions = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        private final Set<UsesNode> uses = new HashSet<>();
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();

        private GroupingDefinitionImpl(final QName qname, final SchemaPath path) {
            this.qname = qname;
            this.path = path;
        }

        @Override
        public QName getQName() {
            return qname;
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
        public boolean isAddedByUses() {
            return addedByUses;
        }

        @Override
        public Set<DataSchemaNode> getChildNodes() {
            return Collections.unmodifiableSet(childNodes);
        }

        private void addChildNodes(Set<DataSchemaNode> childNodes) {
            if (childNodes != null) {
                this.childNodes.addAll(childNodes);
            }
        }

        @Override
        public Set<GroupingDefinition> getGroupings() {
            return Collections.unmodifiableSet(groupings);
        }

        private void addGroupings(Set<GroupingDefinition> groupings) {
            if (groupings != null) {
                this.groupings.addAll(groupings);
            }
        }

        @Override
        public Set<UsesNode> getUses() {
            return Collections.unmodifiableSet(uses);
        }

        private void addUses(Set<UsesNode> uses) {
            if (uses != null) {
                this.uses.addAll(uses);
            }
        }

        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return Collections.unmodifiableSet(typeDefinitions);
        }

        private void addTypeDefinitions(Set<TypeDefinition<?>> typeDefinitions) {
            if (typeDefinitions != null) {
                this.typeDefinitions.addAll(typeDefinitions);
            }
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return Collections.unmodifiableList(unknownNodes);
        }

        private void addUnknownSchemaNodes(List<UnknownSchemaNode> unknownNodes) {
            if (unknownNodes != null) {
                this.unknownNodes.addAll(unknownNodes);
            }
        }

        @Override
        public DataSchemaNode getDataChildByName(QName name) {
            return getChildNode(childNodes, name);
        }

        @Override
        public DataSchemaNode getDataChildByName(String name) {
            return getChildNode(childNodes, name);
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
