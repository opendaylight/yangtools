/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Utility class for adapting {@link NormalizedNodeStreamWriter}s to their {@link DataSchemaNodeAware} counterparts
 * which ignore the provided schema node.
 */
@Beta
public final class DataSchemaNodeAwareAdaptor {
    private DataSchemaNodeAwareAdaptor() {
        throw new UnsupportedOperationException();
    }

    public static SchemaAwareNormalizedNodeStreamAttributeWriter forAttributeWriter(
            @Nonnull final NormalizedNodeStreamAttributeWriter writer) {
        Preconditions.checkNotNull(writer, "Writer must not be null");

        if (writer instanceof SchemaAwareNormalizedNodeStreamAttributeWriter) {
            return (SchemaAwareNormalizedNodeStreamAttributeWriter) writer;
        }

        return new AttributeWriter() {
            @Override
            protected NormalizedNodeStreamAttributeWriter delegate() {
                return writer;
            }
        };
    }

    public static SchemaAwareNormalizedNodeStreamWriter forWriter(@Nonnull final NormalizedNodeStreamWriter writer) {
        Preconditions.checkNotNull(writer, "Writer must not be null");

        if (writer instanceof SchemaAwareNormalizedNodeStreamWriter) {
            return (SchemaAwareNormalizedNodeStreamWriter) writer;
        }
        if (writer instanceof NormalizedNodeStreamAttributeWriter) {
            return forAttributeWriter((NormalizedNodeStreamAttributeWriter) writer);
        }

        return new Writer() {
            @Override
            protected NormalizedNodeStreamWriter delegate() {
                return writer;
            }
        };
    }

    private static abstract class AttributeWriter extends ForwardingNormalizedNodeStreamAttributeWriter
            implements SchemaAwareNormalizedNodeStreamAttributeWriter {
        @Override
        public void nextDataSchemaNode(final DataSchemaNode schema) {
            // Intentional no-op
        }
    }

    private static abstract class Writer extends ForwardingNormalizedNodeStreamWriter
            implements SchemaAwareNormalizedNodeStreamWriter {
        @Override
        public void nextDataSchemaNode(final DataSchemaNode schema) {
            // Intentional no-op
        }
    }
}
