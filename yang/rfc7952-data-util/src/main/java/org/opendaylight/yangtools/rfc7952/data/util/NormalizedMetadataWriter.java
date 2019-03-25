/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadata;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadataStreamWriter;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;

/**
 * A utility class to attach {@link NormalizedMetadata} into a NormalizedNode stream, such as the one produced by
 * {@link NormalizedNodeWriter}, so that a target {@link NormalizedNodeStreamWriter} sees both data and metadata in
 * the stream. A typical use would like this:
 *
 * <p>
 * <code>
 *   // Data for output
 *   NormalizedNode&lt;?, ?&gt; data;
 *   // Metadata for output
 *   NormalizedMetadata metadata;
 *
 *   // Target output writer
 *   NormalizedNodeStreamWriter output = ...;
 *   // Metadata writer
 *   NormalizedMetadataStreamWriter metaWriter = NormalizedMetadataWriter.forStreamWriter(output);
 *
 *   // Write a normalized node and its metadata
 *   dataWriter.write(data, metadata);
 * </code>
 *
 * <p>
 * This class is NOT thread-safe.
 *
 * @author Robert Varga
 */
@Beta
public final class NormalizedMetadataWriter implements Closeable, Flushable {
    private final NormalizedNodeStreamWriter writer;
    private final boolean orderKeyLeaves;

    private NormalizedMetadataWriter(final NormalizedNodeStreamWriter writer, final boolean orderKeyLeaves) {
        this.writer = requireNonNull(writer);
        this.orderKeyLeaves = orderKeyLeaves;
    }

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}. Unlike the simple
     * {@link #forStreamWriter(NormalizedNodeStreamWriter)} method, this allows the caller to switch off RFC6020 XML
     * compliance, providing better throughput. The reason is that the XML mapping rules in RFC6020 require
     * the encoding to emit leaf nodes which participate in a list's key first and in the order in which they are
     * defined in the key. For JSON, this requirement is completely relaxed and leaves can be ordered in any way we
     * see fit. The former requires a bit of work: first a lookup for each key and then for each emitted node we need
     * to check whether it was already emitted.
     *
     * @param writer Back-end writer
     * @param orderKeyLeaves whether the returned instance should be RFC6020 XML compliant.
     * @return A new instance.
     */
    public static @NonNull NormalizedMetadataWriter forStreamWriter(final NormalizedNodeStreamWriter writer,
            final boolean orderKeyLeaves) {
        return new NormalizedMetadataWriter(writer, orderKeyLeaves);
    }

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}. This is a convenience method for
     * {@code forStreamWriter(writer, true)}.
     *
     * @param writer Back-end writer
     * @return A new instance.
     */
    public static @NonNull NormalizedMetadataWriter forStreamWriter(final NormalizedNodeStreamWriter writer) {
        return forStreamWriter(writer, true);
    }

    /**
     * Iterate over the provided {@link NormalizedNode} and {@link NormalizedMetadata} and emit write events to the
     * encapsulated {@link NormalizedNodeStreamWriter}.
     *
     * @param data NormalizedNode data
     * @param metadata {@link NormalizedMetadata} metadata
     * @return NormalizedNodeWriter this
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if metadata does not match data
     * @throws IOException when thrown from the backing writer.
     */
    public @NonNull NormalizedMetadataWriter write(final NormalizedNode<?, ?> data, final NormalizedMetadata metadata)
            throws IOException {
        final PathArgument dataId = data.getIdentifier();
        final PathArgument metaId = metadata.getIdentifier();
        checkArgument(dataId.equals(metaId), "Mismatched data %s and metadata %s", dataId, metaId);

        final NormalizedMetadataStreamWriter metaWriter = writer.getExtensions()
                .getInstance(NormalizedMetadataStreamWriter.class);
        final NormalizedNodeStreamWriter delegate = metaWriter == null ? writer
                : new NormalizedNodeStreamWriterMetadataDecorator(writer, metaWriter, metadata);

        final NormalizedNodeWriter nnWriter = NormalizedNodeWriter.forStreamWriter(delegate, orderKeyLeaves);
        nnWriter.write(data);
        nnWriter.flush();
        return this;
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }
}
