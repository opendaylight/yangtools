/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.builder.impl;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

final class IdentitySchemaNodeImpl implements IdentitySchemaNode {
    private final QName qname;
    private final SchemaPath path;
    IdentitySchemaNode baseIdentity;
    private final Set<IdentitySchemaNode> derivedIdentities;
    String description;
    String reference;
    Status status;
    ImmutableList<UnknownSchemaNode> unknownNodes;

    IdentitySchemaNodeImpl(final QName qname, final SchemaPath path,
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
        sb.append("base=").append(baseIdentity);
        sb.append(", qname=").append(qname);
        sb.append("]");
        return sb.toString();
    }
}