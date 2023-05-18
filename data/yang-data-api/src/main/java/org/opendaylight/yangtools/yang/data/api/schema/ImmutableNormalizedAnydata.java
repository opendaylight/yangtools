/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedMetadataWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;

@NonNullByDefault
sealed class ImmutableNormalizedAnydata implements NormalizedAnydata {
    static final class WithMetadata extends ImmutableNormalizedAnydata implements MetadataNormalizedAnydata {
        private final NormalizedMetadata metadata;

        WithMetadata(final EffectiveStatementInference inference, final NormalizedNode data,
                final NormalizedMetadata metadata) {
            super(inference, data);
            this.metadata = requireNonNull(metadata);
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

    private final EffectiveStatementInference inference;
    private final NormalizedNode data;

    ImmutableNormalizedAnydata(final EffectiveStatementInference inference, final NormalizedNode data) {
        this.inference = requireNonNull(inference);
        this.data = requireNonNull(data);
    }

    @Override
    public EffectiveStatementInference getInference() {
        return inference;
    }

    @Override
    public NormalizedNode getData() {
        return data;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("inference", inference).add("data", data);
    }
}
