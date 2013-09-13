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
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;

public final class UnknownSchemaNodeBuilder extends AbstractSchemaNodeBuilder {
    private boolean isBuilt;
    private final UnknownSchemaNodeImpl instance;
    private boolean addedByUses;
    private QName nodeType;
    private String nodeParameter;

    private ExtensionDefinition extensionDefinition;
    private ExtensionBuilder extensionBuilder;

    public UnknownSchemaNodeBuilder(final String moduleName, final int line, final QName qname) {
        super(moduleName, line, qname);
        instance = new UnknownSchemaNodeImpl(qname);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (addedByUses ? 1 : 0);
        result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
        result = prime * result + ((nodeParameter == null) ? 0 : nodeParameter.hashCode());
        result = prime * result + ((extensionDefinition == null) ? 0 : extensionDefinition.hashCode());
        result = prime * result + ((extensionBuilder == null) ? 0 : extensionBuilder.hashCode());

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
        UnknownSchemaNodeBuilder other = (UnknownSchemaNodeBuilder) obj;

        if (addedByUses != other.addedByUses) {
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
        if (extensionDefinition == null) {
            if (other.extensionDefinition != null) {
                return false;
            }
        } else if (!extensionDefinition.equals(other.extensionDefinition)) {
            return false;
        }
        if (extensionBuilder == null) {
            if (other.extensionBuilder != null) {
                return false;
            }
        } else if (!extensionBuilder.equals(other.extensionBuilder)) {
            return false;
        }

        return true;
    }

    @Override
    public UnknownSchemaNode build() {
        if (!isBuilt) {
            instance.setPath(schemaPath);
            instance.setNodeType(nodeType);
            instance.setNodeParameter(nodeParameter);
            instance.setDescription(description);
            instance.setReference(reference);
            instance.setStatus(status);
            instance.setAddedByUses(addedByUses);

            // EXTENSION
            if (extensionDefinition != null) {
                instance.setExtensionDefinition(extensionDefinition);
            } else {
                if (extensionBuilder != null) {
                    instance.setExtensionDefinition(extensionBuilder.build());
                }
            }

            // UNKNOWN NODES
            if (unknownNodes == null) {
                unknownNodes = new ArrayList<UnknownSchemaNode>();
                for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                    unknownNodes.add(b.build());
                }
                Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            }
            instance.setUnknownSchemaNodes(unknownNodes);

            isBuilt = true;
        }

        return instance;
    }

    public boolean isAddedByUses() {
        return addedByUses;
    }

    public void setAddedByUses(final boolean addedByUses) {
        this.addedByUses = addedByUses;
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

    private final class UnknownSchemaNodeImpl implements UnknownSchemaNode {
        private final QName qname;
        private SchemaPath path;
        private ExtensionDefinition extension;
        private String description;
        private String reference;
        private Status status = Status.CURRENT;
        private List<UnknownSchemaNode> unknownNodes = Collections.emptyList();
        private QName nodeType;
        private String nodeParameter;
        private boolean addedByUses;

        private UnknownSchemaNodeImpl(final QName qname) {
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

        private void setPath(final SchemaPath path) {
            this.path = path;
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

        private void setDescription(final String description) {
            this.description = description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        private void setReference(final String reference) {
            this.reference = reference;
        }

        @Override
        public Status getStatus() {
            return status;
        }

        private void setStatus(final Status status) {
            if (status != null) {
                this.status = status;
            }
        }

        @Override
        public boolean isAddedByUses() {
            return addedByUses;
        }

        private void setAddedByUses(final boolean addedByUses) {
            this.addedByUses = addedByUses;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return unknownNodes;
        }

        private void setUnknownSchemaNodes(final List<UnknownSchemaNode> unknownNodes) {
            if (unknownNodes != null) {
                this.unknownNodes = unknownNodes;
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
            result = prime * result + ((extension == null) ? 0 : extension.hashCode());
            result = prime * result + ((description == null) ? 0 : description.hashCode());
            result = prime * result + ((reference == null) ? 0 : reference.hashCode());
            result = prime * result + status.ordinal();
            result = prime * result + ((unknownNodes == null) ? 0 : unknownNodes.hashCode());
            result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
            result = prime * result + ((nodeParameter == null) ? 0 : nodeParameter.hashCode());
            result = prime * result + (addedByUses ? 1 : 0);

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

            if (addedByUses != other.addedByUses) {
                return false;
            }
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
            if (extension == null) {
                if (other.extension != null) {
                    return false;
                }
            } else if (!extension.equals(other.extension)) {
                return false;
            }
            if (description == null) {
                if (other.description != null) {
                    return false;
                }
            } else if (!description.equals(other.description)) {
                return false;
            }
            if (reference == null) {
                if (other.reference != null) {
                    return false;
                }
            } else if (!reference.equals(other.reference)) {
                return false;
            }
            if (status == null) {
                if (other.status != null) {
                    return false;
                }
            } else if (!status.equals(other.status)) {
                return false;
            }
            if (unknownNodes == null) {
                if (other.unknownNodes != null) {
                    return false;
                }
            } else if (!unknownNodes.equals(other.unknownNodes)) {
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
