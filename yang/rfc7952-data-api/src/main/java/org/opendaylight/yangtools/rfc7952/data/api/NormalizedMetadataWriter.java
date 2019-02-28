/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import javax.annotation.concurrent.NotThreadSafe;
import org.eclipse.jdt.annotation.NonNull;
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
 *   NormalizedMetadataStreamWriter metaWriter = NormalizedMetadataStreamWriter.forStreamWriter(output, metadata);
 *   // Data writer
 *   NormalizedNodeWriter dataWriter = NormalizedNodeWriter.forStreamWriter(metaWriter));
 *
 *   // Write a normalized node
 *   dataWriter.write(data);
 * </code>
 *
 * @author Robert Varga
 */
@Beta
@NotThreadSafe
public class NormalizedMetadataWriter implements Closeable, Flushable {
    private static final class WithMetadata extends NormalizedMetadataWriter {
        private final NormalizedNodeStreamMetadataWriter metaWriter;

        WithMetadata(final NormalizedNodeStreamWriter delegate, final boolean orderKeyLeaves,
                final NormalizedNodeStreamMetadataWriter metaWriter) {
            super(delegate, orderKeyLeaves);
            this.metaWriter = requireNonNull(metaWriter);
        }
    }

    private final NormalizedNodeStreamWriter delegate;
    private final boolean orderKeyLeaves;

    NormalizedMetadataWriter(final NormalizedNodeStreamWriter delegate, final boolean orderKeyLeaves) {
        this.delegate = requireNonNull(delegate);
        this.orderKeyLeaves = orderKeyLeaves;
    }

    public static @NonNull NormalizedMetadataWriter forStreamWriter(final NormalizedNodeStreamWriter writer,
            final boolean orderKeyLeaves) {
        final NormalizedNodeStreamMetadataWriter metaWriter = writer.getExtensions()
                .getInstance(NormalizedNodeStreamMetadataWriter.class);
        final NormalizedMetadataWriter ret = metaWriter != null ? new WithMetadata(writer, orderKeyLeaves, metaWriter)
                : new NormalizedMetadataWriter(writer, orderKeyLeaves);
        return ret;
    }

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
    public NormalizedMetadataWriter write(final NormalizedNode<?, ?> data, final NormalizedMetadata metadata)
            throws IOException {


        return this;
    }

    @Override
    public void close() throws IOException {
        delegate.flush();
        delegate.close();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }



//    @Override
//    protected NormalizedNodeStreamWriter delegate() {
//        return delegate;
//    }
//
//    @Override
//    public void startLeafNode(final NodeIdentifier name) throws IOException {
//        super.startLeafNode(name);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
//        super.startLeafSet(name, childSizeHint);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
//        super.startOrderedLeafSet(name, childSizeHint);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
//        super.startLeafSetEntryNode(name);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
//        super.startContainerNode(name, childSizeHint);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IOException {
//        super.startUnkeyedList(name, childSizeHint);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
//        super.startUnkeyedListItem(name, childSizeHint);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
//        super.startMapNode(name, childSizeHint);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
//            throws IOException {
//        super.startMapEntryNode(identifier, childSizeHint);
//        enterMetadataNode(identifier);
//    }
//
//    @Override
//    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
//        super.startOrderedMapNode(name, childSizeHint);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
//        super.startChoiceNode(name, childSizeHint);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startAugmentationNode(final AugmentationIdentifier identifier) throws IOException {
//        super.startAugmentationNode(identifier);
//        enterMetadataNode(identifier);
//    }
//
//    @Override
//    public void startAnyxmlNode(final NodeIdentifier name) throws IOException {
//        super.startAnyxmlNode(name);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
//        super.startYangModeledAnyXmlNode(name, childSizeHint);
//        enterMetadataNode(name);
//    }
//
//    @Override
//    public void endNode() throws IOException {
//        // TODO Auto-generated method stub
//
//    }
//
//    private void enterMetadataNode(final PathArgument name) {
//        // TODO Auto-generated method stub
//
//    }
}
