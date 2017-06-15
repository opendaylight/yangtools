/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.Objects;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Utility abstract class for tracking parser state, as needed by StAX-like parser.
 * This class is to be used only by respective XML and JSON parsers in yang-data-codec-xml and yang-data-codec-gson.
 */
@Beta
public abstract class AbstractNodeDataWithSchema {
    private final DataSchemaNode schema;

    public AbstractNodeDataWithSchema(final DataSchemaNode schema) {
        this.schema = Preconditions.checkNotNull(schema);
    }

    /**
     * Return the associated schema node.
     *
     * @return Associated schema node.
     */
    public final DataSchemaNode getSchema() {
        return schema;
    }

    /**
     * Emit this node's events into the specified writer.
     *
     * @param writer Target writer
     * @throws IOException reported when thrown by the writer.
     */
    public abstract void write(NormalizedNodeStreamWriter writer) throws IOException;

    protected final NodeIdentifier provideNodeIdentifier() {
        return NodeIdentifier.create(schema.getQName());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(schema);
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
        final AbstractNodeDataWithSchema other = (AbstractNodeDataWithSchema) obj;
        return schema.equals(other.schema);
    }

}
