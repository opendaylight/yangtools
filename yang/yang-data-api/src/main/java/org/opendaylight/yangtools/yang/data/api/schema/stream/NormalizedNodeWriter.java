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
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an experimental iterator over a {@link NormalizedNode}. This is essentially
 * the opposite of a {@link XMLStreamReader} -- unlike instantiating an iterator over
 * the backing data, this encapsulates a {@link NormalizedNodeStreamWriter} and allows
 * us to write multiple nodes.
 */
@Beta
public final class NormalizedNodeWriter implements Closeable, Flushable {
    private static final Logger LOG = LoggerFactory.getLogger(NormalizedNodeWriter.class);
    private final NormalizedNodeStreamWriter writer;

    private NormalizedNodeWriter(final NormalizedNodeStreamWriter writer) {
        this.writer = Preconditions.checkNotNull(writer);
    }

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}.
     *
     * @param writer Backend writer
     * @return A new instance.
     */
    public static NormalizedNodeWriter forStreamWriter(final NormalizedNodeStreamWriter writer) {
        return new NormalizedNodeWriter(writer);
    }

    /**
     * Iterate over the provided {@link NormalizedNode} and emit write
     * events to the encapsulated {@link NormalizedNodeStreamWriter}.
     *
     * @param node Node
     * @return
     * @throws IOException when thrown from the backing writer.
     */
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


    private boolean writeChildren(final Iterable<? extends NormalizedNode<?, ?>> children) throws IOException {
        for (NormalizedNode<?, ?> child : children) {
            write(child);
        }

        writer.endNode();
        return true;
    }

    private boolean wasProcessedAsCompositeNode(final NormalizedNode<?, ?> node) throws IOException {
        if (node instanceof ContainerNode) {
            final ContainerNode n = (ContainerNode) node;
            writer.startContainerNode(n.getIdentifier(), UNKNOWN_SIZE);
            return writeChildren(n.getValue());
        }
        if (node instanceof MapEntryNode) {
            final MapEntryNode n = (MapEntryNode) node;
            writer.startMapEntryNode(n.getIdentifier(), UNKNOWN_SIZE);

            // Reconstruct the QName -> Node mapping
            final Map<QName, NormalizedNode<?, ?>> keys = new HashMap<>();
            final Set<QName> qnames = n.getIdentifier().getKeyValues().keySet();
            for (DataContainerChild<? extends PathArgument, ?> child : n.getValue()) {
                // Break as soon as we have all children
                if (qnames.size() == keys.size()) {
                    break;
                }

                // Augmentations do not have a QName
                final PathArgument pa = child.getIdentifier();
                if (!(pa instanceof AugmentationIdentifier)) {
                    final QName qname = pa.getNodeType();
                    if (qnames.contains(qname)) {
                        keys.put(qname, child);
                    }
                }
            }

            // Write out all the key children
            for (QName qname : qnames) {
                final NormalizedNode<?, ?> child = keys.get(qname);
                if (child != null) {
                    write(child);
                } else {
                    LOG.debug("No child for key element {} found", qname);
                }
            }

            // Write all the rest
            return writeChildren(Iterables.filter(n.getValue(), new Predicate<NormalizedNode<?, ?>>() {
                @Override
                public boolean apply(final NormalizedNode<?, ?> input) {
                    return input instanceof AugmentationNode || !qnames.contains(input.getNodeType());
                }
            }));
        }
        if (node instanceof UnkeyedListEntryNode) {
            final UnkeyedListEntryNode n = (UnkeyedListEntryNode) node;
            writer.startUnkeyedListItem(n.getIdentifier(), UNKNOWN_SIZE);
            return writeChildren(n.getValue());
        }
        if (node instanceof ChoiceNode) {
            final ChoiceNode n = (ChoiceNode) node;
            writer.startChoiceNode(n.getIdentifier(), UNKNOWN_SIZE);
            return writeChildren(n.getValue());
        }
        if (node instanceof AugmentationNode) {
            final AugmentationNode n = (AugmentationNode) node;
            writer.startAugmentationNode(n.getIdentifier());
            return writeChildren(n.getValue());
        }
        if (node instanceof UnkeyedListNode) {
            final UnkeyedListNode n = (UnkeyedListNode) node;
            writer.startUnkeyedList(n.getIdentifier(), UNKNOWN_SIZE);
            return writeChildren(n.getValue());
        }
        if (node instanceof OrderedMapNode) {
            final OrderedMapNode n = (OrderedMapNode) node;
            writer.startOrderedMapNode(n.getIdentifier(), UNKNOWN_SIZE);
            return writeChildren(n.getValue());
        }
        if (node instanceof MapNode) {
            final MapNode n = (MapNode) node;
            writer.startMapNode(n.getIdentifier(), UNKNOWN_SIZE);
            return writeChildren(n.getValue());
        }
        if (node instanceof LeafSetNode) {
            //covers also OrderedLeafSetNode for which doesn't exist start* method
            final LeafSetNode<?> n = (LeafSetNode<?>) node;
            writer.startLeafSet(n.getIdentifier(), UNKNOWN_SIZE);
            return writeChildren(n.getValue());
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
