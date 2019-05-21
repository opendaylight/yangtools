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
import java.util.List;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataStreamWriter;
import org.opendaylight.yangtools.rfc7952.data.api.OpaqueAnydataStreamWriter;
import org.opendaylight.yangtools.rfc7952.data.api.OpaqueMetadata;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueData;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataContainer;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataList;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataNode;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueDataValue;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.OpaqueAnydataExtension;
import org.opendaylight.yangtools.yang.data.api.schema.stream.OpaqueAnydataExtension.StreamWriter;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;

@Beta
public final class OpaqueAnydataNodeDataWithSchema extends AbstractAnydataNodeDataWithSchema<OpaqueData> {
    public OpaqueAnydataNodeDataWithSchema(final AnyDataSchemaNode dataSchemaNode) {
        super(dataSchemaNode);
    }

    @Override
    protected Class<OpaqueData> objectModelClass() {
        return OpaqueData.class;
    }

    @Override
    protected void write(final NormalizedNodeStreamWriter writer, final NormalizedMetadataStreamWriter metaWriter)
            throws IOException {
        final OpaqueAnydataExtension ext = writer.getExtensions().getInstance(OpaqueAnydataExtension.class);
        if (ext != null) {
            writer.nextDataSchemaNode(getSchema());
            streamOpaqueAnydataNode(ext, provideNodeIdentifier(), getValue(), getMetadata());
            writer.endNode();
        }
    }

    private static void streamOpaqueAnydataNode(final OpaqueAnydataExtension ext, final NodeIdentifier name,
            final OpaqueData data, final OpaqueMetadata metadata) throws IOException {
        if (metadata != null) {
            final StreamWriter dataWriter = ext.startOpaqueAnydataNode(name, data.hasAccurateLists());
            final OpaqueDataNode node = data.getRoot();
            if (dataWriter instanceof OpaqueAnydataStreamWriter) {
                streamOpaqueDataNode((OpaqueAnydataStreamWriter) dataWriter, node, metadata);
            } else {
                dataWriter.streamOpaqueDataNode(node);
            }
        } else {
            ext.streamOpaqueAnydataNode(name, data);
        }
    }

    private static void streamOpaqueDataNode(final OpaqueAnydataStreamWriter writer, final OpaqueDataNode node,
            final OpaqueMetadata metadata) throws IOException {
        if (node instanceof OpaqueDataValue) {
            writer.startOpaqueContainer(node.getIdentifier(), 0);
            writeMetadata(writer, metadata);
            writer.opaqueValue(((OpaqueDataValue) node).getValue());
            writer.endOpaqueNode();
            return;
        }
        final List<? extends OpaqueDataNode> children;
        if (node instanceof OpaqueDataList) {
            children = ((OpaqueDataList) node).getChildren();
            writer.startOpaqueList(node.getIdentifier(), children.size());
        } else if (node instanceof OpaqueDataContainer) {
            children = ((OpaqueDataContainer) node).getChildren();
            writer.startOpaqueContainer(node.getIdentifier(), children.size());
        } else {
            throw new IllegalStateException("Unhandled node " + node);
        }

        writeMetadata(writer, metadata);
        for (OpaqueDataNode child : children) {
            streamOpaqueDataNode(writer, child, metadata.getChildren().get(child.getIdentifier()));
        }
        writer.endOpaqueNode();
    }

    private static void writeMetadata(final OpaqueAnydataStreamWriter writer, final OpaqueMetadata metadata)
            throws IOException {
        if (metadata != null) {
            writer.metadata(ImmutableMap.copyOf(metadata.getAnnotations()));
        }
    }
}
