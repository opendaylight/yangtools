/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.UNKNOWN_SIZE;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.xml.stream.XMLStreamReader;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
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
import org.opendaylight.yangtools.yang.data.api.schema.OrderedLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an experimental iterator over a {@link NormalizedNode}. This is essentially
 * the opposite of a {@link XMLStreamReader} -- unlike instantiating an iterator over
 * the backing data, this encapsulates a {@link NormalizedNodeStreamWriter} and allows
 * us to write multiple nodes.
 */
@Beta
public class NormalizedNodeWriter implements Closeable, Flushable {
    private final NormalizedNodeStreamWriter writer;

    protected NormalizedNodeWriter(final NormalizedNodeStreamWriter writer) {
        this.writer = requireNonNull(writer);
    }

    protected final NormalizedNodeStreamWriter getWriter() {
        return writer;
    }

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}.
     *
     * @param writer Back-end writer
     * @return A new instance.
     */
    public static NormalizedNodeWriter forStreamWriter(final NormalizedNodeStreamWriter writer) {
        return forStreamWriter(writer, true);
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
    public static NormalizedNodeWriter forStreamWriter(final NormalizedNodeStreamWriter writer,
            final boolean orderKeyLeaves) {
        return orderKeyLeaves ? new OrderedNormalizedNodeWriter(writer) : new NormalizedNodeWriter(writer);
    }

    /**
     * Iterate over the provided {@link NormalizedNode} and emit write
     * events to the encapsulated {@link NormalizedNodeStreamWriter}.
     *
     * @param node Node
     * @return NormalizedNodeWriter this
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

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    /**
     * Emit a best guess of a hint for a particular set of children. It evaluates the
     * iterable to see if the size can be easily gotten to. If it is, we hint at the
     * real number of child nodes. Otherwise we emit UNKNOWN_SIZE.
     *
     * @param children Child nodes
     * @return Best estimate of the collection size required to hold all the children.
     */
    protected static int childSizeHint(final Iterable<?> children) {
        return children instanceof Collection ? ((Collection<?>) children).size() : UNKNOWN_SIZE;
    }

    protected boolean wasProcessAsSimpleNode(final NormalizedNode<?, ?> node) throws IOException {
        if (node instanceof LeafSetEntryNode) {
            final LeafSetEntryNode<?> nodeAsLeafList = (LeafSetEntryNode<?>)node;
            final QName name = nodeAsLeafList.getIdentifier().getNodeType();
            if (writer instanceof NormalizedNodeStreamAttributeWriter) {
                ((NormalizedNodeStreamAttributeWriter) writer).leafSetEntryNode(name, nodeAsLeafList.getValue(),
                        nodeAsLeafList.getAttributes());
            } else {
                writer.leafSetEntryNode(name, nodeAsLeafList.getValue());
            }
            return true;
        } else if (node instanceof LeafNode) {
            final LeafNode<?> nodeAsLeaf = (LeafNode<?>)node;
            if (writer instanceof NormalizedNodeStreamAttributeWriter) {
                ((NormalizedNodeStreamAttributeWriter) writer).leafNode(nodeAsLeaf.getIdentifier(),
                    nodeAsLeaf.getValue(), nodeAsLeaf.getAttributes());
            } else {
                writer.leafNode(nodeAsLeaf.getIdentifier(), nodeAsLeaf.getValue());
            }
            return true;
        } else if (node instanceof AnyXmlNode) {
            final AnyXmlNode anyXmlNode = (AnyXmlNode)node;
            writer.anyxmlNode(anyXmlNode.getIdentifier(), anyXmlNode.getValue());
            return true;
        }

        return false;
    }

    /**
     * Emit events for all children and then emit an endNode() event.
     *
     * @param children Child iterable
     * @return True
     * @throws IOException when the writer reports it
     */
    protected boolean writeChildren(final Iterable<? extends NormalizedNode<?, ?>> children) throws IOException {
        for (final NormalizedNode<?, ?> child : children) {
            write(child);
        }

        writer.endNode();
        return true;
    }

    protected boolean writeMapEntryNode(final MapEntryNode node) throws IOException {
        if (writer instanceof NormalizedNodeStreamAttributeWriter) {
            ((NormalizedNodeStreamAttributeWriter) writer)
                    .startMapEntryNode(node.getIdentifier(), childSizeHint(node.getValue()), node.getAttributes());
        } else {
            writer.startMapEntryNode(node.getIdentifier(), childSizeHint(node.getValue()));
        }
        return writeChildren(node.getValue());
    }

    protected boolean wasProcessedAsCompositeNode(final NormalizedNode<?, ?> node) throws IOException {
        if (node instanceof ContainerNode) {
            final ContainerNode n = (ContainerNode) node;
            if (writer instanceof NormalizedNodeStreamAttributeWriter) {
                ((NormalizedNodeStreamAttributeWriter) writer).startContainerNode(n.getIdentifier(),
                    childSizeHint(n.getValue()), n.getAttributes());
            } else {
                writer.startContainerNode(n.getIdentifier(), childSizeHint(n.getValue()));
            }
            return writeChildren(n.getValue());
        }
        if (node instanceof YangModeledAnyXmlNode) {
            final YangModeledAnyXmlNode n = (YangModeledAnyXmlNode) node;
            if (writer instanceof NormalizedNodeStreamAttributeWriter) {
                ((NormalizedNodeStreamAttributeWriter) writer).startYangModeledAnyXmlNode(n.getIdentifier(),
                    childSizeHint(n.getValue()), n.getAttributes());
            } else {
                writer.startYangModeledAnyXmlNode(n.getIdentifier(), childSizeHint(n.getValue()));
            }
            return writeChildren(n.getValue());
        }
        if (node instanceof MapEntryNode) {
            return writeMapEntryNode((MapEntryNode) node);
        }
        if (node instanceof UnkeyedListEntryNode) {
            final UnkeyedListEntryNode n = (UnkeyedListEntryNode) node;
            writer.startUnkeyedListItem(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue());
        }
        if (node instanceof ChoiceNode) {
            final ChoiceNode n = (ChoiceNode) node;
            writer.startChoiceNode(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue());
        }
        if (node instanceof AugmentationNode) {
            final AugmentationNode n = (AugmentationNode) node;
            writer.startAugmentationNode(n.getIdentifier());
            return writeChildren(n.getValue());
        }
        if (node instanceof UnkeyedListNode) {
            final UnkeyedListNode n = (UnkeyedListNode) node;
            writer.startUnkeyedList(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue());
        }
        if (node instanceof OrderedMapNode) {
            final OrderedMapNode n = (OrderedMapNode) node;
            writer.startOrderedMapNode(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue());
        }
        if (node instanceof MapNode) {
            final MapNode n = (MapNode) node;
            writer.startMapNode(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue());
        }
        if (node instanceof OrderedLeafSetNode) {
            final LeafSetNode<?> n = (LeafSetNode<?>) node;
            writer.startOrderedLeafSet(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue());
        }
        if (node instanceof LeafSetNode) {
            final LeafSetNode<?> n = (LeafSetNode<?>) node;
            writer.startLeafSet(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue());
        }

        return false;
    }

    private static final class OrderedNormalizedNodeWriter extends NormalizedNodeWriter {
        private static final Logger LOG = LoggerFactory.getLogger(OrderedNormalizedNodeWriter.class);

        OrderedNormalizedNodeWriter(final NormalizedNodeStreamWriter writer) {
            super(writer);
        }

        @Override
        protected boolean writeMapEntryNode(final MapEntryNode node) throws IOException {
            final NormalizedNodeStreamWriter nnWriter = getWriter();
            if (nnWriter instanceof NormalizedNodeStreamAttributeWriter) {
                ((NormalizedNodeStreamAttributeWriter) nnWriter).startMapEntryNode(node.getIdentifier(),
                    childSizeHint(node.getValue()), node.getAttributes());
            } else {
                nnWriter.startMapEntryNode(node.getIdentifier(), childSizeHint(node.getValue()));
            }

            final Set<QName> qnames = node.getIdentifier().getKeyValues().keySet();
            // Write out all the key children
            for (final QName qname : qnames) {
                final Optional<? extends NormalizedNode<?, ?>> child = node.getChild(new NodeIdentifier(qname));
                if (child.isPresent()) {
                    write(child.get());
                } else {
                    LOG.info("No child for key element {} found", qname);
                }
            }

            // Write all the rest
            return writeChildren(Iterables.filter(node.getValue(), input -> {
                if (input instanceof AugmentationNode) {
                    return true;
                }
                if (!qnames.contains(input.getNodeType())) {
                    return true;
                }

                LOG.debug("Skipping key child {}", input);
                return false;
            }));
        }
    }
}
