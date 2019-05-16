/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.util;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadata;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataStreamWriter;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedAnydataExtension;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.util.AbstractAnydataNodeDataWithSchema;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;

@Beta
@SuppressWarnings("rawtypes")
public final class NormalizedAnydataNodeDataWithSchema extends AbstractAnydataNodeDataWithSchema<NormalizedNode> {
    public NormalizedAnydataNodeDataWithSchema(final AnyDataSchemaNode dataSchemaNode) {
        super(dataSchemaNode);
    }

    @Override
    protected Class<NormalizedNode> objectModelClass() {
        return NormalizedNode.class;
    }

    @Override
    protected void write(final NormalizedNodeStreamWriter writer, final NormalizedMetadataStreamWriter metaWriter)
            throws IOException {
        final NormalizedAnydataExtension ext = writer.getExtensions().getInstance(NormalizedAnydataExtension.class);
        if (ext != null) {
            writer.nextDataSchemaNode(getSchema());
            streamNormalizedAnydataNode(ext, provideNodeIdentifier(), getValue(), getMetadata());
            writer.endNode();
        }
    }

    private static void streamNormalizedAnydataNode(final NormalizedAnydataExtension ext, final NodeIdentifier name,
            final NormalizedNode<?, ?> data, final NormalizedMetadata metadata) throws IOException {
        final NormalizedNodeStreamWriter dataWriter = ext.startNormalizedAnydataNode(name);
        if (metadata != null) {
            NormalizedMetadataWriter.forStreamWriter(dataWriter).write(data, metadata).flush();
        } else {
            NormalizedNodeWriter.forStreamWriter(dataWriter).write(data).flush();
        }
    }
}
