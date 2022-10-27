/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import org.opendaylight.yangtools.rfc7952.data.api.NormalizedMetadata;
import org.opendaylight.yangtools.rfc7952.data.api.StreamWriterMetadataExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.ForwardingNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

/**
 * A simple decorator on top of a NormalizedNodeStreamWriter, which attaches NormalizedMetadata to the event stream,
 * so that the metadata is emitted along with data.
 */
final class NormalizedNodeStreamWriterMetadataDecorator extends ForwardingNormalizedNodeStreamWriter {
    private final Deque<NormalizedMetadata> stack = new ArrayDeque<>();
    private final StreamWriterMetadataExtension metaWriter;
    private final NormalizedNodeStreamWriter writer;
    private final NormalizedMetadata metadata;

    private int absentDepth = 0;

    NormalizedNodeStreamWriterMetadataDecorator(final NormalizedNodeStreamWriter writer,
            final StreamWriterMetadataExtension metaWriter, final NormalizedMetadata metadata) {
        this.writer = requireNonNull(writer);
        this.metaWriter = requireNonNull(metaWriter);
        this.metadata = requireNonNull(metadata);
    }

    @Override
    protected NormalizedNodeStreamWriter delegate() {
        return writer;
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        super.startLeafNode(name);
        enterMetadataNode(name);
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startLeafSet(name, childSizeHint);
        enterMetadataNode(name);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startOrderedLeafSet(name, childSizeHint);
        enterMetadataNode(name);
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        super.startLeafSetEntryNode(name);
        enterMetadataNode(name);
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startContainerNode(name, childSizeHint);
        enterMetadataNode(name);
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startUnkeyedList(name, childSizeHint);
        enterMetadataNode(name);
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startUnkeyedListItem(name, childSizeHint);
        enterMetadataNode(name);
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startMapNode(name, childSizeHint);
        enterMetadataNode(name);
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IOException {
        super.startMapEntryNode(identifier, childSizeHint);
        enterMetadataNode(identifier);
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startOrderedMapNode(name, childSizeHint);
        enterMetadataNode(name);
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        super.startChoiceNode(name, childSizeHint);
        enterMetadataNode(name);
    }

    @Override
    public boolean startAnyxmlNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        final boolean ret = super.startAnyxmlNode(name, objectModel);
        if (ret) {
            enterMetadataNode(name);
        }
        return ret;
    }

    @Override
    public void endNode() throws IOException {
        super.endNode();

        if (absentDepth > 0) {
            absentDepth--;
        } else {
            stack.pop();
        }
    }

    private void enterMetadataNode(final PathArgument name) throws IOException {
        if (absentDepth > 0) {
            absentDepth++;
            return;
        }

        final NormalizedMetadata current = stack.peek();
        if (current != null) {
            final NormalizedMetadata child = current.getChildren().get(name);
            if (child != null) {
                enterChild(child);
            } else {
                absentDepth = 1;
            }
        } else {
            // Empty stack: enter first entry
            enterChild(metadata);
        }
    }

    private void enterChild(final NormalizedMetadata child) throws IOException {
        final Map<QName, Object> annotations = child.getAnnotations();
        if (!annotations.isEmpty()) {
            metaWriter.metadata(ImmutableMap.copyOf(annotations));
        }
        stack.push(child);
    }
}
