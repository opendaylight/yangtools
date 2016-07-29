/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

final class BitImpl implements Bit, Immutable {
    private final List<UnknownSchemaNode> unknownNodes;
    private final SchemaPath schemaPath;
    private final String description;
    private final String reference;
    private final Status status;
    private final Long position;

    BitImpl(final SchemaPath schemaPath, final Long position, final String description,
            final String reference, final Status status, final List<UnknownSchemaNode> unknownNodes) {
        this.schemaPath = Preconditions.checkNotNull(schemaPath, "Schema Path should not be null");
        this.position = Preconditions.checkNotNull(position, "Position should not be null");
        this.description = description;
        this.reference = reference;
        this.status = Preconditions.checkNotNull(status);
        this.unknownNodes = Preconditions.checkNotNull(unknownNodes);
    }

    @Override
    public QName getQName() {
        return schemaPath.getLastComponent();
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
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
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public Long getPosition() {
        return position;
    }

    @Override
    public String getName() {
        return getQName().getLocalName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getQName().hashCode();
        result = prime * result + schemaPath.hashCode();
        result = prime * result + position.hashCode();
        result = prime * result + unknownNodes.hashCode();
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
        Bit other = (Bit) obj;
        return Objects.equals(schemaPath, other.getPath());
    }

    @Override
    public String toString() {
        return Bit.class.getSimpleName() + "[name=" + getQName().getLocalName() + ", position=" + position + "]";
    }

}
