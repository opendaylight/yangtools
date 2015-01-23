/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

final class RpcDefinitionImpl implements RpcDefinition {
    private final QName qname;
    private final SchemaPath path;
    String description;
    String reference;
    Status status;
    private ContainerSchemaNode input;
    private ContainerSchemaNode output;
    ImmutableSet<TypeDefinition<?>> typeDefinitions;
    ImmutableSet<GroupingDefinition> groupings;
    ImmutableList<UnknownSchemaNode> unknownNodes;

    RpcDefinitionImpl(final QName qname, final SchemaPath path) {
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

    void setInput(final ContainerSchemaNode input) {
        this.input = input;
    }

    @Override
    public ContainerSchemaNode getOutput() {
        return output;
    }

    void setOutput(final ContainerSchemaNode output) {
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