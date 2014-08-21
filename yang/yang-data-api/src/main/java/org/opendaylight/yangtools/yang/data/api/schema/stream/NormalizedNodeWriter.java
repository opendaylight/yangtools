/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.UNKNOWN_SIZE;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;

/**
 * This is an experimental
 */
@Beta
public final class NormalizedNodeWriter implements Closeable, Flushable {
    private final NormalizedNodeStreamWriter writer;

    private NormalizedNodeWriter(final NormalizedNodeStreamWriter writer) {
        this.writer = Preconditions.checkNotNull(writer);
    }

    public static NormalizedNodeWriter forStreamWriter(final NormalizedNodeStreamWriter writer) {
        return new NormalizedNodeWriter(writer);
    }

    public NormalizedNodeWriter write(final NormalizedNode<?, ?> node) throws IOException {
        if (wasProcessedAsCompositeNode(node)) {
            return this;
        }

        if (wasProcessAsSimpleNode(node)) {
            return this;
        }

        throw new IllegalStateException("It wasn't possible to serialize node " + node);
    }

    private boolean wasProcessAsSimpleNode(final NormalizedNode<?, ?> node) throws IOException {
        if (node instanceof LeafSetEntryNode) {
            final LeafSetEntryNode<?> nodeAsLeafList = (LeafSetEntryNode<?>)node;
            writer.leafSetEntryNode(nodeAsLeafList.getValue());
            return true;
        } else if (node instanceof LeafNode) {
            final LeafNode<?> nodeAsLeaf = (LeafNode<?>)node;
            writer.leafNode(nodeAsLeaf.getIdentifier(), nodeAsLeaf.getValue());
            return true;
        } else if (node instanceof AnyXmlNode) {
            final AnyXmlNode anyXmlNode = (AnyXmlNode)node;
            writer.anyxmlNode(anyXmlNode.getIdentifier(), anyXmlNode.getValue());
            return true;
        }

        return false;
    }

    private boolean wasProcessedAsCompositeNode(final NormalizedNode<?, ?> node) throws IOException {
        boolean hasDataContainerChild = false;
        if (node instanceof ContainerNode) {
            writer.startContainerNode(((ContainerNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof MapEntryNode) {
            writer.startMapEntryNode(((MapEntryNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof UnkeyedListEntryNode) {
            writer.startUnkeyedListItem(((UnkeyedListEntryNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof ChoiceNode) {
            writer.startChoiceNode(((ChoiceNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof AugmentationNode) {
            writer.startAugmentationNode(((AugmentationNode) node).getIdentifier());
            hasDataContainerChild = true;
        } else if (node instanceof UnkeyedListNode) {
            writer.startUnkeyedList(((UnkeyedListNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof OrderedMapNode) {
            writer.startOrderedMapNode(((OrderedMapNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof MapNode) {
            writer.startMapNode(((MapNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
          //covers also OrderedLeafSetNode for which doesn't exist start* method
        } else if (node instanceof LeafSetNode) {
            writer.startLeafSet(((LeafSetNode<?>) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        }

        if (hasDataContainerChild) {
            for (NormalizedNode<?, ?> childNode : ((NormalizedNode<?, Iterable<NormalizedNode<?, ?>>>) node).getValue()) {
                write(childNode);
            }

            writer.endNode();
            return true;
        }
        return false;

    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
