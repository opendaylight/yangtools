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
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;

public final class UnknownSchemaNodeBuilder extends AbstractSchemaNodeBuilder {
    private boolean isBuilt;
    private final UnknownSchemaNodeImpl instance;
    private QName nodeType;
    private String nodeParameter;

    private ExtensionDefinition extensionDefinition;
    private ExtensionBuilder extensionBuilder;

    public UnknownSchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        instance = new UnknownSchemaNodeImpl(qname, path);
    }

    public UnknownSchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path, final UnknownSchemaNode base) {
        super(moduleName, line, base.getQName());
        this.schemaPath = path;
        instance = new UnknownSchemaNodeImpl(qname, path);

        instance.nodeType = base.getNodeType();
        instance.nodeParameter = base.getNodeParameter();
        instance.description = base.getDescription();
        instance.reference = base.getReference();
        instance.status = base.getStatus();
        instance.addedByUses = base.isAddedByUses();
        instance.extension = base.getExtensionDefinition();
        instance.unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public SchemaPath getPath() {
        return instance.path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qname == null) ? 0 : qname.hashCode());
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
        result = prime * result + ((nodeParameter == null) ? 0 : nodeParameter.hashCode());
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
        UnknownSchemaNodeBuilder other = (UnknownSchemaNodeBuilder) obj;
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
        if (!isBuilt) {
            instance.setNodeType(nodeType);
            instance.setNodeParameter(nodeParameter);

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

            isBuilt = true;
        }

        return instance;
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

    public boolean isAddedByUses() {
        return instance.addedByUses;
    }

    public void setAddedByUses(final boolean addedByUses) {
        instance.addedByUses = addedByUses;
    }

    public QName getNodeType() {
        return nodeType;
    }

    public void setNodeType(final QName nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeParameter() {
        return nodeParameter;
    }

    public void setNodeParameter(final String nodeParameter) {
        this.nodeParameter = nodeParameter;
    }

    public ExtensionDefinition getExtensionDefinition() {
        return extensionDefinition;
    }

    public void setExtensionDefinition(final ExtensionDefinition extensionDefinition) {
        this.extensionDefinition = extensionDefinition;
    }

    public ExtensionBuilder getExtensionBuilder() {
        return extensionBuilder;
    }

    public void setExtensionBuilder(final ExtensionBuilder extension) {
        this.extensionBuilder = extension;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeType.getPrefix());
        sb.append(":");
        sb.append(nodeType.getLocalName());
        sb.append(" ");
        sb.append(nodeParameter);
        return sb.toString();
    }

    private static final class UnknownSchemaNodeImpl implements UnknownSchemaNode {
        private final QName qname;
        private final SchemaPath path;
        private ExtensionDefinition extension;
        private String description;
        private String reference;
        private Status status = Status.CURRENT;
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();
        private QName nodeType;
        private String nodeParameter;
        private boolean addedByUses;

        private UnknownSchemaNodeImpl(final QName qname, final SchemaPath path) {
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

        private void setExtensionDefinition(final ExtensionDefinition extension) {
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

        private void setUnknownSchemaNodes(final List<UnknownSchemaNode> unknownNodes) {
            if (unknownNodes != null) {
                this.unknownNodes.addAll(unknownNodes);
            }
        }

        @Override
        public QName getNodeType() {
            return nodeType;
        }

        private void setNodeType(final QName nodeType) {
            this.nodeType = nodeType;
        }

        @Override
        public String getNodeParameter() {
            return nodeParameter;
        }

        private void setNodeParameter(final String nodeParameter) {
            this.nodeParameter = nodeParameter;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(nodeType.getPrefix());
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
            result = prime * result + ((qname == null) ? 0 : qname.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
            result = prime * result + ((nodeParameter == null) ? 0 : nodeParameter.hashCode());
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

    }

}
