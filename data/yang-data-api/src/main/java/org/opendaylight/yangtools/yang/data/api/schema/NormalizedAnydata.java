/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

/**
 * The contents of an {@code anydata} node in a normalized format. This representation acts as a schema-bound bridge
 * between the various (mostly parser-based) representations. Implementations of this interface are usually created
 * from an instance of {@link NormalizableAnydata}.
 *
 * <p>
 * Note this interface does not have an equality contract and implementations are expected to default to identity
 * equality (or in Valhalla-speak: be plain data).
 */
@Beta
@NonNullByDefault
public interface NormalizedAnydata extends Immutable {
    /**
     * Return the {@link EffectiveStatementInference} which describes the structure returned by {@link #getData()}.
     *
     * @return An {@link EffectiveStatementInference}
     */
    EffectiveStatementInference getInference();

    /**
     * Return the {@link NormalizedNode} representation of this node's data. Information about the corresponding schema
     * is available via {@link #getInference()}.
     *
     * @return A {@link NormalizedNode}
     */
    NormalizedNode getData();

    default void writeTo(final NormalizedNodeStreamWriter writer) throws IOException {
        writeTo(writer, true);
    }

    default void writeTo(final NormalizedNodeStreamWriter writer, final boolean orderKeyLeaves) throws IOException {
        new NormalizedNodeWriter(writer, !orderKeyLeaves).write(getData()).flush();
    }

    static NormalizedAnydata of(final EffectiveStatementInference inference, final NormalizedNode data) {
        return new ImmutableNormalizedAnydata(inference, data);
    }

    static NormalizedAnydata of(final EffectiveStatementInference inference, final NormalizedNode data,
            final @Nullable NormalizedMetadata metadata) {
        return metadata != null ? MetadataNormalizedAnydata.of(inference, data, metadata) : of(inference, data);
    }
}
