/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

final class UnknownSchemaNodeImpl implements UnknownSchemaNode {
    private final QName qname;
    private final SchemaPath path;
    private ExtensionDefinition extension;
    String description;
    String reference;
    Status status = Status.CURRENT;
    private List<UnknownSchemaNode> unknownNodes = ImmutableList.of();
    private QName nodeType;
    private String nodeParameter;
    boolean addedByUses;

    UnknownSchemaNodeImpl(final QName qname, final SchemaPath path) {
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
    public ExtensionDefinition getExtensionDefinition() {
        return extension;
    }

    void setExtensionDefinition(final ExtensionDefinition extension) {
        this.extension = extension;
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
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    void setUnknownSchemaNodes(final List<UnknownSchemaNode> unknownNodes) {
        if (unknownNodes != null) {
            this.unknownNodes = ImmutableList.copyOf(unknownNodes);
        }
    }

    @Override
    public QName getNodeType() {
        return nodeType;
    }

    void setNodeType(final QName nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public String getNodeParameter() {
        return nodeParameter;
    }

    void setNodeParameter(final String nodeParameter) {
        this.nodeParameter = nodeParameter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeType.getNamespace());
        sb.append(":");
        sb.append(nodeType.getLocalName());
        sb.append(" ");
        sb.append(nodeParameter);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(qname);
        result = prime * result + Objects.hashCode(path);
        result = prime * result + Objects.hashCode(nodeType);
        result = prime * result + Objects.hashCode(nodeParameter);
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
        UnknownSchemaNodeImpl other = (UnknownSchemaNodeImpl) obj;
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
        if (nodeType == null) {
            if (other.nodeType != null) {
                return false;
            }
        } else if (!nodeType.equals(other.nodeType)) {
            return false;
        }
        if (nodeParameter == null) {
            if (other.nodeParameter != null) {
                return false;
            }
        } else if (!nodeParameter.equals(other.nodeParameter)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isAddedByAugmentation() {
        throw new UnsupportedOperationException();
    }
}
