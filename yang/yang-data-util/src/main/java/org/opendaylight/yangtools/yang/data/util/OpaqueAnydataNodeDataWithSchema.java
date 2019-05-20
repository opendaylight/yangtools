/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueData;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.OpaqueAnydataExtension;
import org.opendaylight.yangtools.yang.data.api.schema.stream.OpaqueAnydataExtension.StreamWriter;
import org.opendaylight.yangtools.yang.model.api.AnyDataSchemaNode;

@Beta
public class OpaqueAnydataNodeDataWithSchema extends AbstractAnydataNodeDataWithSchema<OpaqueData> {
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
            final OpaqueData value = getValue();
            final StreamWriter opaqueWriter = ext.startOpaqueAnydataNode(provideNodeIdentifier(),
                value.hasAccurateLists());


            // FIXME: write out
            //          writeMetadata(metaWriter);

            writer.endNode();
        }
    }
}
