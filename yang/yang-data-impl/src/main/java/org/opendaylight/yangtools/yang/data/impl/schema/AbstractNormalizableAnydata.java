/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.AnydataNormalizationException;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.data.util.NormalizableAnydata;
import org.opendaylight.yangtools.yang.data.util.NormalizedAnydata;

/**
 * Abstract base class for implementing the NormalizableAnydata interface. This class provides the binding to
 * NormalizedNodeStreamWriter.
 */
@Beta
@NonNullByDefault
public abstract class AbstractNormalizableAnydata implements NormalizableAnydata {
    @Override
    public final NormalizedAnydata normalizeTo(final DataSchemaContextTree contextTree,
            final DataSchemaContextNode<?> contextNode) throws AnydataNormalizationException {
        final NormalizedNodeMetadataResult result = new NormalizedNodeMetadataResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        try {
            writeTo(streamWriter, contextTree, contextNode);
        } catch (IOException e) {
            throw new AnydataNormalizationException("Failed to normalize anydata", e);
        }

        return new NormalizedAnydata(contextTree, contextNode, result.getResult());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected abstract ToStringHelper addToStringAttributes(ToStringHelper helper);

    protected abstract void writeTo(NormalizedNodeStreamWriter streamWriter, DataSchemaContextTree contextTree,
            DataSchemaContextNode<?> contextNode) throws IOException;
}
