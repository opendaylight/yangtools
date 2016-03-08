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
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.ExtensionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;

/**
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
public final class UnknownSchemaNodeBuilderImpl extends AbstractBuilder implements UnknownSchemaNodeBuilder {
    private final QName qname;
    private SchemaPath schemaPath;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    private boolean addedByUses;

    private UnknownSchemaNodeImpl instance;
    private QName nodeType;
    private String nodeParameter;

    private ExtensionDefinition extensionDefinition;
    private ExtensionBuilder extensionBuilder;

    public UnknownSchemaNodeBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line);
        this.qname = qname;
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
    }

    public UnknownSchemaNodeBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path, final UnknownSchemaNode base) {
        super(moduleName, line);
        this.qname = base.getQName();
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");

        this.nodeType = base.getNodeType();
        this.nodeParameter = base.getNodeParameter();
        this.description = base.getDescription();
        this.reference = base.getReference();
        this.status = base.getStatus();
        this.addedByUses = base.isAddedByUses();
        this.extensionDefinition = base.getExtensionDefinition();
        this.unknownNodes.addAll(base.getUnknownSchemaNodes());
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
    public void setPath(final SchemaPath schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(qname);
        result = prime * result + Objects.hashCode(schemaPath);
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
        UnknownSchemaNodeBuilderImpl other = (UnknownSchemaNodeBuilderImpl) obj;
        if (qname == null) {
            if (other.qname != null) {
                return false;
            }
        } else if (!qname.equals(other.qname)) {
            return false;
        }
        if (schemaPath == null) {
            if (other.schemaPath != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.schemaPath)) {
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
    public UnknownSchemaNode build() {
        if (instance != null) {
            return instance;
        }

        instance = new UnknownSchemaNodeImpl(qname, schemaPath);

        instance.setNodeType(nodeType);
        instance.setNodeParameter(nodeParameter);

        instance.description = description;
        instance.reference = reference;
        instance.status = status;
        instance.addedByUses = addedByUses;

        // EXTENSION
        if (extensionDefinition != null) {
            instance.setExtensionDefinition(extensionDefinition);
        } else {
            if (extensionBuilder != null) {
                instance.setExtensionDefinition(extensionBuilder.build());
            }
        }

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
        instance.setUnknownSchemaNodes(unknownNodes);

        return instance;
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
    public QName getNodeType() {
        return nodeType;
    }

    @Override
    public void setNodeType(final QName nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public String getNodeParameter() {
        return nodeParameter;
    }

    @Override
    public void setNodeParameter(final String nodeParameter) {
        this.nodeParameter = nodeParameter;
    }

    @Override
    public ExtensionDefinition getExtensionDefinition() {
        return extensionDefinition;
    }

    @Override
    public void setExtensionDefinition(final ExtensionDefinition extensionDefinition) {
        this.extensionDefinition = extensionDefinition;
    }

    @Override
    public ExtensionBuilder getExtensionBuilder() {
        return extensionBuilder;
    }

    @Override
    public void setExtensionBuilder(final ExtensionBuilder extension) {
        this.extensionBuilder = extension;
    }

    @Override
    public String toString() {
        return String.valueOf(nodeType.getNamespace()) +
                ":" +
                nodeType.getLocalName() +
                " " +
                nodeParameter;
    }

}
