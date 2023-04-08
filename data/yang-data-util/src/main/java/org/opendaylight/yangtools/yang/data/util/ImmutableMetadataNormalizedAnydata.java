/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.MetadataNormalizedAnydata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedMetadata;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

@Beta
@NonNullByDefault
public final class ImmutableMetadataNormalizedAnydata extends ImmutableNormalizedAnydata
        implements MetadataNormalizedAnydata {
    private final NormalizedMetadata metadata;

    public ImmutableMetadataNormalizedAnydata(final EffectiveStatementInference inference, final NormalizedNode data,
            final NormalizedMetadata metadata) {
        super(inference, data);
        this.metadata = requireNonNull(metadata);
    }

    public static ImmutableNormalizedAnydata ofOptional(final EffectiveStatementInference inference,
            final NormalizedNode data, final Optional<NormalizedMetadata> metadata) {
        return metadata.isPresent()
                ? new ImmutableMetadataNormalizedAnydata(inference, data, metadata.orElseThrow())
                        : new ImmutableNormalizedAnydata(inference, data);
    }

    @Override
    public NormalizedMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void writeTo(final NormalizedNodeStreamWriter writer, final boolean orderKeyLeaves) throws IOException {
        NormalizedMetadataWriter.forStreamWriter(writer, orderKeyLeaves).write(getData(), getMetadata()).flush();
    }
}
