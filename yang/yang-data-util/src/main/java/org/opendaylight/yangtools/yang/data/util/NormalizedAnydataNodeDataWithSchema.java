/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadata;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataStreamWriter;
import org.opendaylight.yangtools.rfc7952.data.api.OpaqueAnydataStreamWriter;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedAnydataExtension;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;

@Beta
@SuppressWarnings("rawtypes")
public class NormalizedAnydataNodeDataWithSchema extends AbstractAnydataNodeDataWithSchema<NormalizedNode> {
    protected NormalizedAnydataNodeDataWithSchema(final AnyDataSchemaNode dataSchemaNode) {
        super(dataSchemaNode);
    }

    @Override
    protected final Class<NormalizedNode> objectModelClass() {
        return NormalizedNode.class;
    }

    @Override
    protected final void write(final NormalizedNodeStreamWriter writer, final NormalizedMetadataStreamWriter metaWriter)
            throws IOException {
        final NormalizedAnydataExtension ext = writer.getExtensions().getInstance(NormalizedAnydataExtension.class);
        if (ext != null) {
            writer.nextDataSchemaNode(getSchema());
            streamNormalizedAnydataNode(ext, provideNodeIdentifier(), getValue(), getMetadata());
            writer.endNode();
        }
    }

    private void streamNormalizedAnydataNode(final NormalizedAnydataExtension ext, final NodeIdentifier name,
            final NormalizedNode<?, ?> data, final NormalizedMetadata metadata) throws IOException {
        final NormalizedNodeStreamWriter dataWriter = ext.startNormalizedAnydataNode(name);


        if (metadata != null) {

            // FIXME: propagate metadata
            NormalizedNodeWriter.forStreamWriter(dataWriter).write(data).flush();
        } else {
            writeData(dataWriter, data);
            dataWriter.write(data).flush();
            NormalizedNodeWriter.forStreamWriter(dataWriter).write(data).flush();
        }
    }

    protected void writeData(final NormalizedNodeStreamWriter dataWriter, final NormalizedNode<?, ?> data,
            final NormalizedMetadata metadata) throws IOException {
        NormalizedNodeWriter.forStreamWriter(dataWriter).write(data).flush();
    }

    private static void writeMetadata(final OpaqueAnydataStreamWriter writer, final NormalizedMetadata metadata)
            throws IOException {
        if (metadata != null) {
            writer.metadata(ImmutableMap.copyOf(metadata.getAnnotations()));
        }
    }
}
