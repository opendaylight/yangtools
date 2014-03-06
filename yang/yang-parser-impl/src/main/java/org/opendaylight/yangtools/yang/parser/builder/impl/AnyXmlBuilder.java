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
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;

public final class AnyXmlBuilder extends AbstractSchemaNodeBuilder implements DataSchemaNodeBuilder {
    private boolean built;
    private final AnyXmlSchemaNodeImpl instance;
    private final ConstraintsBuilder constraints;

    public AnyXmlBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        instance = new AnyXmlSchemaNodeImpl(qname, schemaPath);
        constraints = new ConstraintsBuilder(moduleName, line);
    }

    public AnyXmlBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path,
            final AnyXmlSchemaNode base) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        this.instance = new AnyXmlSchemaNodeImpl(qname, schemaPath);
        constraints = new ConstraintsBuilder(moduleName, line, base.getConstraints());

        instance.description = base.getDescription();
        instance.reference = base.getReference();
        instance.status = base.getStatus();
        instance.augmenting = base.isAugmenting();
        instance.addedByUses = base.isAddedByUses();
        instance.configuration = base.isConfiguration();
        instance.constraintsDef = base.getConstraints();
        instance.unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public AnyXmlSchemaNode build() {
        if (!built) {
            instance.setConstraints(constraints.build());

            // UNKNOWN NODES
            for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
                unknownNodes.add(b.build());
            }
            Collections.sort(unknownNodes, Comparators.SCHEMA_NODE_COMP);
            instance.setUnknownSchemaNodes(unknownNodes);

            built = true;
        }
        return instance;
    }

    @Override
    public SchemaPath getPath() {
        return instance.path;
    }

    @Override
    public void setPath(SchemaPath path) {
        instance.path = path;
    }

    @Override
    public ConstraintsBuilder getConstraints() {
        return constraints;
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

    public List<UnknownSchemaNodeBuilder> getUnknownNodes() {
        return addedUnknownNodes;
    }

    @Override
    public boolean isAugmenting() {
        return instance.augmenting;
    }

    @Override
    public void setAugmenting(final boolean augmenting) {
        instance.augmenting = augmenting;
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
    public boolean isConfiguration() {
        return instance.configuration;
    }

    @Override
    public void setConfiguration(final boolean configuration) {
        instance.configuration = configuration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        AnyXmlBuilder other = (AnyXmlBuilder) obj;
        if (schemaPath == null) {
            if (other.schemaPath != null) {
                return false;
            }
        } else if (!schemaPath.equals(other.schemaPath)) {
            return false;
        }
        if (parentBuilder == null) {
            if (other.parentBuilder != null) {
                return false;
            }
        } else if (!parentBuilder.equals(other.parentBuilder)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "anyxml " + qname.getLocalName();
    }

    private static final class AnyXmlSchemaNodeImpl implements AnyXmlSchemaNode {
        private final QName qname;
        private SchemaPath path;
        private String description;
        private String reference;
        private Status status = Status.CURRENT;
        private boolean configuration;
        private ConstraintDefinition constraintsDef;
        private boolean augmenting;
        private boolean addedByUses;
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();

        private AnyXmlSchemaNodeImpl(final QName qname, final SchemaPath path) {
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
        public boolean isAugmenting() {
            return augmenting;
        }

        @Override
        public boolean isAddedByUses() {
            return addedByUses;
        }

        @Override
        public boolean isConfiguration() {
            return configuration;
        }

        @Override
        public ConstraintDefinition getConstraints() {
            return constraintsDef;
        }

        private void setConstraints(ConstraintDefinition constraintsDef) {
            this.constraintsDef = constraintsDef;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return Collections.unmodifiableList(unknownNodes);
        }

        private void setUnknownSchemaNodes(List<UnknownSchemaNode> unknownNodes) {
            if (unknownNodes != null) {
                this.unknownNodes.addAll(unknownNodes);
            }
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
            AnyXmlSchemaNodeImpl other = (AnyXmlSchemaNodeImpl) obj;
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
            StringBuilder sb = new StringBuilder(AnyXmlSchemaNodeImpl.class.getSimpleName());
            sb.append("[");
            sb.append("qname=" + qname);
            sb.append(", path=" + path);
            sb.append("]");
            return sb.toString();
        }
    }

}
