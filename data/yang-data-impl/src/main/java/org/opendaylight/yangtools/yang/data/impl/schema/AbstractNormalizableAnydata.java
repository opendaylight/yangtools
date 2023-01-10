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
import org.opendaylight.yangtools.rfc7952.data.util.ImmutableMetadataNormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNormalizationException;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizableAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.util.ImmutableNormalizedAnydata;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 * Abstract base class for implementing the NormalizableAnydata interface. This class provides the binding to
 * NormalizedNodeStreamWriter.
 */
@Beta
@NonNullByDefault
public abstract class AbstractNormalizableAnydata implements NormalizableAnydata {
    @Override
    public final ImmutableNormalizedAnydata normalizeTo(final EffectiveStatementInference inference)
            throws AnydataNormalizationException {
        final NormalizedNodeMetadataResult result = new NormalizedNodeMetadataResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        try {
            writeTo(streamWriter, inference);
        } catch (IOException e) {
            throw new AnydataNormalizationException("Failed to normalize anydata", e);
        }
        final NormalizedNode data = result.getResult();
        if (data == null) {
            throw new AnydataNormalizationException("No anydata content found to normalize");
        }
        return ImmutableMetadataNormalizedAnydata.ofOptional(inference, data, result.getMetadata());
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected abstract ToStringHelper addToStringAttributes(ToStringHelper helper);

    protected abstract void writeTo(NormalizedNodeStreamWriter streamWriter, EffectiveStatementInference inference)
        throws IOException;
}
