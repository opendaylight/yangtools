/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.ExtensionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.Comparators;

public final class UnknownSchemaNodeBuilderImpl extends AbstractSchemaNodeBuilder implements UnknownSchemaNodeBuilder {
    private boolean isBuilt;
    private final UnknownSchemaNodeImpl instance;
    private QName nodeType;
    private String nodeParameter;

    private ExtensionDefinition extensionDefinition;
    private ExtensionBuilder extensionBuilder;

    public UnknownSchemaNodeBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
        instance = new UnknownSchemaNodeImpl(qname, path);
    }

    public UnknownSchemaNodeBuilderImpl(final String moduleName, final int line, final QName qname, final SchemaPath path, final UnknownSchemaNode base) {
        super(moduleName, line, base.getQName());
        this.schemaPath = Preconditions.checkNotNull(path, "Schema Path must not be null");
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

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#getPath()
     */
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

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#build()
     */
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

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#getDescription()
     */
    @Override
    public String getDescription() {
        return instance.description;
    }

    @Override
    public void setDescription(final String description) {
        instance.description = description;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#getReference()
     */
    @Override
    public String getReference() {
        return instance.reference;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#setReference(java.lang.String)
     */
    @Override
    public void setReference(final String reference) {
        instance.reference = reference;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#getStatus()
     */
    @Override
    public Status getStatus() {
        return instance.status;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#setStatus(org.opendaylight.yangtools.yang.model.api.Status)
     */
    @Override
    public void setStatus(final Status status) {
        if (status != null) {
            instance.status = status;
        }
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#isAddedByUses()
     */
    @Override
    public boolean isAddedByUses() {
        return instance.addedByUses;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#setAddedByUses(boolean)
     */
    @Override
    public void setAddedByUses(final boolean addedByUses) {
        instance.addedByUses = addedByUses;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#getNodeType()
     */
    @Override
    public QName getNodeType() {
        return nodeType;
    }

    @Override
    public void setNodeType(final QName nodeType) {
        this.nodeType = nodeType;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#getNodeParameter()
     */
    @Override
    public String getNodeParameter() {
        return nodeParameter;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#setNodeParameter(java.lang.String)
     */
    @Override
    public void setNodeParameter(final String nodeParameter) {
        this.nodeParameter = nodeParameter;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#getExtensionDefinition()
     */
    @Override
    public ExtensionDefinition getExtensionDefinition() {
        return extensionDefinition;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#setExtensionDefinition(org.opendaylight.yangtools.yang.model.api.ExtensionDefinition)
     */
    @Override
    public void setExtensionDefinition(final ExtensionDefinition extensionDefinition) {
        this.extensionDefinition = extensionDefinition;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#getExtensionBuilder()
     */
    @Override
    public ExtensionBuilder getExtensionBuilder() {
        return extensionBuilder;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.yangtools.yang.parser.builder.impl.IUnkownSchemaNodeBuilder#setExtensionBuilder(org.opendaylight.yangtools.yang.parser.builder.impl.ExtensionBuilder)
     */
    @Override
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

    }

}
