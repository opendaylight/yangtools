/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.NormalizedAnydata;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
@NonNullByDefault
public final class MetadataNormalizedAnydata extends NormalizedAnydata {
    private final NormalizedMetadata metadata;

    public MetadataNormalizedAnydata(final SchemaContext schemaContext, final DataSchemaNode contextNode,
            final NormalizedNode<?, ?> data, final NormalizedMetadata metadata) {
        super(schemaContext, contextNode, data);
        this.metadata = requireNonNull(metadata);
    }

    public NormalizedMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void writeTo(final NormalizedNodeStreamWriter writer, final boolean orderKeyLeaves) throws IOException {
        NormalizedMetadataWriter.forStreamWriter(writer, orderKeyLeaves).write(getData(), metadata).flush();
    }
}
