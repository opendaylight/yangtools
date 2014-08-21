/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.serialization;

import static org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.UNKNOWN_SIZE;

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
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;

public class NormalizedNodeToStreamWriter {

    public void serialize(final NormalizedNode<?, ?> node, final NormalizedNodeStreamWriter nnsw) {
        if (wasProcessedAsCompositeNode(node, nnsw)) {
            return;
        }

        if (wasProcessAsSimpleNode(node, nnsw)) {
            return;
        }
        throw new IllegalStateException("It wasn't possible to serialize node "+node);
    }

    private boolean wasProcessAsSimpleNode(final NormalizedNode<?, ?> node, final NormalizedNodeStreamWriter nnsw) {
        if (node instanceof LeafSetEntryNode) {
            final LeafSetEntryNode<?> nodeAsLeafList = (LeafSetEntryNode<?>)node;
            nnsw.leafSetEntryNode(nodeAsLeafList.getValue());
            return true;
        } else if (node instanceof LeafNode) {
            final LeafNode<?> nodeAsLeaf = (LeafNode<?>)node;
            nnsw.leafNode(nodeAsLeaf.getIdentifier(), nodeAsLeaf.getValue());
            return true;
        } else if (node instanceof AnyXmlNode) {
            final AnyXmlNode anyXmlNode = (AnyXmlNode)node;
            nnsw.anyxmlNode(anyXmlNode.getIdentifier(), anyXmlNode.getValue());
            return true;
        }

        return false;
    }

    private boolean wasProcessedAsCompositeNode(final NormalizedNode<?, ?> node, final NormalizedNodeStreamWriter nnsw) {
        boolean hasDataContainerChild = false;
        if (node instanceof ContainerNode) {
            nnsw.startContainerNode(((ContainerNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof MapEntryNode) {
            nnsw.startMapEntryNode(((MapEntryNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof UnkeyedListEntryNode) {
            nnsw.startUnkeyedListItem(((UnkeyedListEntryNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof ChoiceNode) {
            nnsw.startChoiceNode(((ChoiceNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof AugmentationNode) {
            nnsw.startAugmentationNode(((AugmentationNode) node).getIdentifier());
            hasDataContainerChild = true;
        } else if (node instanceof UnkeyedListNode) {
            nnsw.startUnkeyedList(((UnkeyedListNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof OrderedMapNode) {
            nnsw.startOrderedMapNode(((OrderedMapNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        } else if (node instanceof MapNode) {
            nnsw.startMapNode(((MapNode) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
          //covers also OrderedLeafSetNode for which doesn't exist start* method
        } else if (node instanceof LeafSetNode) {
            nnsw.startLeafSet(((LeafSetNode<?>) node).getIdentifier(), UNKNOWN_SIZE);
            hasDataContainerChild = true;
        }

        if (hasDataContainerChild) {
            for (NormalizedNode<?, ?> childNode : ((NormalizedNode<?, Iterable<NormalizedNode<?, ?>>>) node).getValue()) {
                serialize(childNode, nnsw);
            }

            nnsw.endNode();
            return true;
        }
        return false;

    }

}
