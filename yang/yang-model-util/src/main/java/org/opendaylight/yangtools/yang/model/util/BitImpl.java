/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

public final class BitImpl implements BitsTypeDefinition.Bit, Immutable {
    private final Long position;
    private final QName qname;
    private final SchemaPath schemaPath;
    private final String description;
    private final String reference;
    private final Status status;
    private List<UnknownSchemaNode> unknownNodes = Collections.emptyList();

    public BitImpl(final Long position, final QName qname, final SchemaPath schemaPath, final String description,
            final String reference, final Status status, final List<UnknownSchemaNode> unknownNodes) {
        this.position = Preconditions.checkNotNull(position, "Position should not be null");
        this.qname = Preconditions.checkNotNull(qname, "QName should not be null");
        this.schemaPath = Preconditions.checkNotNull(schemaPath, "Schema Path should not be null");
        this.description = description;
        this.reference = reference;
        this.status = status;
        if (unknownNodes != null) {
            this.unknownNodes = unknownNodes;
        }
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
        return qname.getLocalName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + qname.hashCode();
        result = prime * result + schemaPath.hashCode();
        result = prime * result + position.hashCode();
        result = prime * result + Objects.hashCode(unknownNodes);
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
        return Objects.equals(qname, other.getQName()) && Objects.equals(schemaPath, other.getPath());
    }

    @Override
    public String toString() {
        return Bit.class.getSimpleName() + "[name=" + qname.getLocalName() + ", position=" + position + "]";
    }

}
