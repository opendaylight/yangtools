/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;

/**
 * Basic implementation of SchemaNodeBuilder.
 */
public abstract class AbstractSchemaNodeBuilder extends AbstractBuilder implements SchemaNodeBuilder {
    protected final QName qname;
    protected SchemaPath schemaPath;
    protected String description;
    protected String reference;
    protected Status status = Status.CURRENT;
    protected List<UnknownSchemaNode> unknownNodes;

    protected AbstractSchemaNodeBuilder(final String moduleName, final int line, final QName qname) {
        super(moduleName, line);
        this.qname = qname;
    }

    @Override
    public int hashCode() {
        final int prime = super.hashCode();
        int result = 1;
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((reference == null) ? 0 : reference.hashCode());
        result = prime * result + status.ordinal();
        // result = prime * result + ((unknownNodes == null) ? 0 :
        // unknownNodes.hashCode());

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
        AbstractSchemaNodeBuilder other = (AbstractSchemaNodeBuilder) obj;

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

        return true;
    }

    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public void setPath(SchemaPath schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        if (status != null) {
            this.status = status;
        }
    }

    public void setUnknownNodes(List<UnknownSchemaNode> unknownNodes) {
        this.unknownNodes = unknownNodes;
    }

}
