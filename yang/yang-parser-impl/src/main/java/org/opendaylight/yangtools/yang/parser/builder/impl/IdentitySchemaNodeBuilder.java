/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.util.AbstractSchemaNodeBuilder;

public final class IdentitySchemaNodeBuilder extends AbstractSchemaNodeBuilder {
    private IdentitySchemaNodeImpl instance;
    private IdentitySchemaNode baseIdentity;
    private IdentitySchemaNodeBuilder baseIdentityBuilder;
    private final Set<IdentitySchemaNode> derivedIdentities = new HashSet<>();
    private String baseIdentityName;

    IdentitySchemaNodeBuilder(final String moduleName, final int line, final QName qname, final SchemaPath path) {
        super(moduleName, line, qname);
        schemaPath = path;
    }

    IdentitySchemaNodeBuilder(final String moduleName, IdentitySchemaNode base) {
        super(moduleName, 0, base.getQName());
        schemaPath = base.getPath();
        derivedIdentities.addAll(base.getDerivedIdentities());
        unknownNodes.addAll(base.getUnknownSchemaNodes());
    }

    @Override
    public IdentitySchemaNode build() {
        if (instance != null) {
            return instance;
        }

        instance = new IdentitySchemaNodeImpl(qname, schemaPath, derivedIdentities);

        instance.description = description;
        instance.reference = reference;
        instance.status = status;

        if (baseIdentityBuilder != null) {
            baseIdentityBuilder.addDerivedIdentity(instance);
            baseIdentity = baseIdentityBuilder.build();
        }
        instance.baseIdentity = baseIdentity;

        // UNKNOWN NODES
        for (UnknownSchemaNodeBuilder b : addedUnknownNodes) {
            unknownNodes.add(b.build());
        }
        instance.unknownNodes = ImmutableList.copyOf(unknownNodes);

        return instance;
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

    public void addDerivedIdentity(IdentitySchemaNode derivedIdentity) {
        if (derivedIdentity != null) {
            derivedIdentities.add(derivedIdentity);
        }
    }

    @Override
    public String toString() {
        return "identity " + qname.getLocalName();
    }

    private static final class IdentitySchemaNodeImpl implements IdentitySchemaNode {
        private final QName qname;
        private final SchemaPath path;
        private IdentitySchemaNode baseIdentity;
        private final Set<IdentitySchemaNode> derivedIdentities;
        private String description;
        private String reference;
        private Status status;
        private ImmutableList<UnknownSchemaNode> unknownNodes;

        private IdentitySchemaNodeImpl(final QName qname, final SchemaPath path,
                final Set<IdentitySchemaNode> derivedIdentities) {
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
