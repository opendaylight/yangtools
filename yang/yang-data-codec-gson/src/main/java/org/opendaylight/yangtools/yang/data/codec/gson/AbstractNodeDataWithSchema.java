/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class AbstractNodeDataWithSchema {

    private final DataSchemaNode schema;

    public AbstractNodeDataWithSchema(final DataSchemaNode schema) {
        this.schema = schema;
    }

    public final DataSchemaNode getSchema() {
        return schema;
    }

    protected abstract void writeToStream(final NormalizedNodeStreamWriter nnStreamWriter);

    protected NodeIdentifier provideNodeIdentifier() {
        return new NodeIdentifier(schema.getQName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
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
        AbstractNodeDataWithSchema other = (AbstractNodeDataWithSchema) obj;
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }

        return true;
    }

}
