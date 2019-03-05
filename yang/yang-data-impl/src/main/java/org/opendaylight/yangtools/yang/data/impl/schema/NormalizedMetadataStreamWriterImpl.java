/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataStreamWriter;
import org.opendaylight.yangtools.rfc7952.data.util.ImmutableNormalizedMetadata;
import org.opendaylight.yangtools.rfc7952.data.util.ImmutableNormalizedMetadata.Builder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ForwardingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;
import org.opendaylight.yangtools.yang.data.api.schema.stream.PruningNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.PruningNormalizedNodeStreamWriter.Filter;

final class NormalizedMetadataStreamWriterImpl extends ForwardingNormalizedNodeStreamWriter
        implements Filter, NormalizedMetadataStreamWriter {
    private final NormalizedNodeResult dataResult = new NormalizedNodeResult();
    private final Deque<Builder> builders = new ArrayDeque<>();
    private final PruningNormalizedNodeStreamWriter delegate;
    private final NormalizedNodeMetadataResult result;

    NormalizedMetadataStreamWriterImpl(final NormalizedNodeMetadataResult result) {
        this.delegate = new PruningNormalizedNodeStreamWriter(ImmutableNormalizedNodeStreamWriter.from(dataResult),
            this);
        this.result = requireNonNull(result);
    }

    @Override
    protected NormalizedNodeStreamWriter delegate() {
        return delegate;
    }

    @Override
    public ClassToInstanceMap<NormalizedNodeStreamWriterExtension> getExtensions() {
        return ImmutableClassToInstanceMap.of(NormalizedMetadataStreamWriter.class, this);
    }

    @Override
    public void metadata(final ImmutableMap<QName, Object> metadata) throws IOException {
        final Builder current = builders.peek();
        checkState(current != null, "Attempted to emit metadata when no metadata is open");
        current.withAnnotations(metadata);
    }

    @Override
    public boolean enterNode(final PathArgument name) {
        builders.push(ImmutableNormalizedMetadata.builder().withIdentifier(name));
        return true;
    }

    @Override
    public void exitNode() {
        final ImmutableNormalizedMetadata metadata = builders.pop().build();
        final Builder current = builders.peek();
        if (current == null) {
            // All done
            result.setResult(dataResult.getResult(), metadata);
        } else {
            current.withChild(metadata);
        }
    }
}
