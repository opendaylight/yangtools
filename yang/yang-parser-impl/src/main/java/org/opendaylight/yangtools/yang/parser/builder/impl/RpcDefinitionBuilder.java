/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public final class RpcDefinitionBuilder extends AbstractSchemaNodeBuilder {
    private RpcDefinitionImpl instance;
    private ContainerSchemaNodeBuilder inputBuilder;
    private ContainerSchemaNodeBuilder outputBuilder;
    private final Set<TypeDefinitionBuilder> addedTypedefs = new HashSet<>();
    private final Set<GroupingBuilder> addedGroupings = new HashSet<>();

    public ContainerSchemaNodeBuilder getInput() {
        return inputBuilder;
    }

    public ContainerSchemaNodeBuilder getOutput() {
        return outputBuilder;
    }

    RpcDefinitionBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
    }

    @Override
    public RpcDefinition build() {
        if (instance != null) {
            return instance;
        }

        instance = new RpcDefinitionImpl(qname, schemaPath);

        final ContainerSchemaNode input = inputBuilder == null ? null : inputBuilder.build();
        final ContainerSchemaNode output = outputBuilder == null ? null : outputBuilder.build();
        instance.setInput(input);
        instance.setOutput(output);

        instance.description = description;
        instance.reference = reference;
        instance.status = status;

        // TYPEDEFS
        final Set<TypeDefinition<?>> typedefs = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        for (TypeDefinitionBuilder entry : addedTypedefs) {
            typedefs.add(entry.build());
        }
        instance.typeDefinitions = ImmutableSet.copyOf(typedefs);

        // GROUPINGS
        final Set<GroupingDefinition> groupings = new TreeSet<>(Comparators.SCHEMA_NODE_COMP);
        for (GroupingBuilder builder : addedGroupings) {
            groupings.add(builder.build());
        }
        instance.groupings = ImmutableSet.copyOf(groupings);

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
    }

    void setInput(final ContainerSchemaNodeBuilder inputBuilder) {
        this.inputBuilder = inputBuilder;
    }

    void setOutput(final ContainerSchemaNodeBuilder outputBuilder) {
        this.outputBuilder = outputBuilder;
    }

    public Set<TypeDefinitionBuilder> getTypeDefinitions() {
        return addedTypedefs;
    }

    public void addTypedef(final TypeDefinitionBuilder type) {
        addedTypedefs.add(type);
    }

    public Set<GroupingBuilder> getGroupings() {
        return addedGroupings;
    }

    public void addGrouping(final GroupingBuilder grouping) {
        addedGroupings.add(grouping);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qname == null) ? 0 : qname.hashCode());
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof RpcDefinitionBuilder)) {
            return false;
        }
        final RpcDefinitionBuilder other = (RpcDefinitionBuilder) obj;
        if (other.qname == null) {
            if (this.qname != null) {
                return false;
            }
        } else if (!other.qname.equals(this.qname)) {
            return false;
        }
        if (other.schemaPath == null) {
            if (this.schemaPath != null) {
                return false;
            }
        } else if (!other.schemaPath.equals(this.schemaPath)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "rpc " + qname.getLocalName();
    }

    private static final class RpcDefinitionImpl implements RpcDefinition {
        private final QName qname;
        private final SchemaPath path;
        private String description;
        private String reference;
        private Status status;
        private ContainerSchemaNode input;
        private ContainerSchemaNode output;
        private ImmutableSet<TypeDefinition<?>> typeDefinitions;
        private ImmutableSet<GroupingDefinition> groupings;
        private ImmutableList<UnknownSchemaNode> unknownNodes;

        private RpcDefinitionImpl(final QName qname, final SchemaPath path) {
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
        public ContainerSchemaNode getInput() {
            return input;
        }

        private void setInput(final ContainerSchemaNode input) {
            this.input = input;
        }

        @Override
        public ContainerSchemaNode getOutput() {
            return output;
        }

        private void setOutput(final ContainerSchemaNode output) {
            this.output = output;
        }

        @Override
        public Set<TypeDefinition<?>> getTypeDefinitions() {
            return typeDefinitions;
        }

        @Override
        public Set<GroupingDefinition> getGroupings() {
            return groupings;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
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
            final RpcDefinitionImpl other = (RpcDefinitionImpl) obj;
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
            StringBuilder sb = new StringBuilder(RpcDefinitionImpl.class.getSimpleName());
            sb.append("[");
            sb.append("qname=");
            sb.append(qname);
            sb.append(", path=");
            sb.append(path);
            sb.append(", input=");
            sb.append(input);
            sb.append(", output=");
            sb.append(output);
            sb.append("]");
            return sb.toString();
        }
    }

}
