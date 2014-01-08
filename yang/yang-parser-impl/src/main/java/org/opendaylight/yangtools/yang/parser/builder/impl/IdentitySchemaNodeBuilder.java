/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.*;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;
import org.opendaylight.yangtools.yang.parser.builder.api.AbstractSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.Comparators;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

public final class IdentitySchemaNodeBuilder extends AbstractSchemaNodeBuilder {
    private boolean isBuilt;
    private final IdentitySchemaNodeImpl instance;
    private IdentitySchemaNode baseIdentity;
    private IdentitySchemaNodeBuilder baseIdentityBuilder;
    private final Set<IdentitySchemaNode> derivedIdentities = new HashSet<>();
    private String baseIdentityName;

    IdentitySchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        this.schemaPath = path;
        instance = new IdentitySchemaNodeImpl(qname, path, derivedIdentities);
    }

    @Override
    public IdentitySchemaNode build() {
        if (!isBuilt) {
            if (!(parentBuilder instanceof ModuleBuilder)) {
                throw new YangParseException(moduleName, line, "Identity can be defined only under module (was" + parentBuilder + ")");
            }
            if (baseIdentity == null) {
                if(baseIdentityBuilder != null) {
                    baseIdentityBuilder.addDerivedIdentity(instance);
                    baseIdentity = baseIdentityBuilder.build();
                }
            } else {
                if(baseIdentity instanceof IdentitySchemaNodeImpl) {
                    ((IdentitySchemaNodeImpl)baseIdentity).toBuilder().addDerivedIdentity(instance);
                }
            }
            instance.setBaseIdentity(baseIdentity);

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
    public SchemaPath getPath() {
        return instance.path;
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

    public String getBaseIdentityName() {
        return baseIdentityName;
    }

    public void setBaseIdentityName(final String baseIdentityName) {
        this.baseIdentityName = baseIdentityName;
    }

    public void setBaseIdentity(final IdentitySchemaNodeBuilder baseType) {
        this.baseIdentityBuilder = baseType;
    }

    public void setBaseIdentity(final IdentitySchemaNode baseType) {
        this.baseIdentity = baseType;
    }

    public void addDerivedIdentity(IdentitySchemaNode derivedIdentity) {
        if (derivedIdentity != null) {
            derivedIdentities.add(derivedIdentity);
        }
    }

    @Override
    public String toString() {
        return "identity " + qname.getLocalName();
    }

    public final class IdentitySchemaNodeImpl implements IdentitySchemaNode {
        private final QName qname;
        private final SchemaPath path;
        private IdentitySchemaNode baseIdentity;
        private final Set<IdentitySchemaNode> derivedIdentities;
        private String description;
        private String reference;
        private Status status = Status.CURRENT;
        private final List<UnknownSchemaNode> unknownNodes = new ArrayList<>();

        private IdentitySchemaNodeImpl(final QName qname, final SchemaPath path, final Set<IdentitySchemaNode> derivedIdentities) {
            this.qname = qname;
            this.path = path;
            this.derivedIdentities = derivedIdentities;
        }

        @Override
        public QName getQName() {
            return qname;
        }

        @Override
        public IdentitySchemaNode getBaseIdentity() {
            return baseIdentity;
        }

        private void setBaseIdentity(final IdentitySchemaNode baseIdentity) {
            this.baseIdentity = baseIdentity;
        }

        @Override
        public Set<IdentitySchemaNode> getDerivedIdentities() {
            return Collections.unmodifiableSet(derivedIdentities);
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
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return Collections.unmodifiableList(unknownNodes);
        }

        private void addUnknownSchemaNodes(List<UnknownSchemaNode> unknownSchemaNodes) {
            if (unknownSchemaNodes != null) {
                this.unknownNodes.addAll(unknownSchemaNodes);
            }
        }

        public IdentitySchemaNodeBuilder toBuilder() {
            return IdentitySchemaNodeBuilder.this;
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
            IdentitySchemaNodeImpl other = (IdentitySchemaNodeImpl) obj;
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
            StringBuilder sb = new StringBuilder(IdentitySchemaNodeImpl.class.getSimpleName());
            sb.append("[");
            sb.append("base=" + baseIdentity);
            sb.append(", qname=" + qname);
            sb.append("]");
            return sb.toString();
        }
    }

}
