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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.data.util.NormalizedAnydata;

@Beta
@NonNullByDefault
public final class MetadataNormalizedAnydata extends NormalizedAnydata {
    private final NormalizedMetadata metadata;

    public MetadataNormalizedAnydata(final DataSchemaContextTree contextTree,
            final DataSchemaContextNode<?> contextNode, final NormalizedNode<?, ?> data,
            final NormalizedMetadata metadata) {
        super(contextTree, contextNode, data);
        this.metadata = requireNonNull(metadata);
    }

    public NormalizedMetadata getMetadata() {
        return metadata;
    }
}
