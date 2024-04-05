/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Set;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an experimental iterator over a {@link NormalizedNode}. This is essentially the opposite of a
 * {@link XMLStreamReader} -- unlike instantiating an iterator over the backing data, this encapsulates a
 * {@link NormalizedNodeStreamWriter} and allows us to write multiple nodes.
 */
@Beta
public class NormalizedNodeWriter implements Closeable, Flushable {
    private static final Logger LOG = LoggerFactory.getLogger(NormalizedNodeWriter.class);

    private final @NonNull NormalizedNodeStreamWriter writer;

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
    public NormalizedNodeWriter write(final NormalizedNode node) throws IOException {
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

    protected boolean wasProcessAsSimpleNode(final NormalizedNode node) throws IOException {
        if (node instanceof LeafSetEntryNode<?> nodeAsLeafList) {
            writer.startLeafSetEntryNode(nodeAsLeafList.name());
            writer.scalarValue(nodeAsLeafList.body());
            writer.endNode();
            return true;
        } else if (node instanceof LeafNode<?> nodeAsLeaf) {
            writer.startLeafNode(nodeAsLeaf.name());
            writer.scalarValue(nodeAsLeaf.body());
            writer.endNode();
            return true;
        } else if (node instanceof AnyxmlNode<?> anyxmlNode) {
            final Class<?> model = anyxmlNode.bodyObjectModel();
            if (writer.startAnyxmlNode(anyxmlNode.name(), model)) {
                final Object value = node.body();
                if (DOMSource.class.isAssignableFrom(model)) {
                    verify(value instanceof DOMSource, "Inconsistent anyxml node %s", anyxmlNode);
                    writer.domSourceValue((DOMSource) value);
                } else {
                    writer.scalarValue(value);
                }
                writer.endNode();
                return true;
            }

            LOG.debug("Ignoring unhandled anyxml node {}", anyxmlNode);
        } else if (node instanceof AnydataNode<?> anydata) {
            final Class<?> model = anydata.bodyObjectModel();
            if (writer.startAnydataNode(anydata.name(), model)) {
                writer.scalarValue(anydata.body());
                writer.endNode();
                return true;
            }

            LOG.debug("Writer {} does not support anydata in form of {}", writer, model);
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
    protected boolean writeChildren(final Iterable<? extends NormalizedNode> children) throws IOException {
        for (var child : children) {
            write(child);
        }

        writer.endNode();
        return true;
    }

    protected boolean writeMapEntryNode(final MapEntryNode node) throws IOException {
        writer.startMapEntryNode(node.name(), node.size());
        return writeChildren(node.body());
    }

    protected boolean wasProcessedAsCompositeNode(final NormalizedNode node) throws IOException {
        if (node instanceof ContainerNode n) {
            writer.startContainerNode(n.name(), n.size());
            return writeChildren(n.body());
        } else if (node instanceof MapEntryNode n) {
            return writeMapEntryNode(n);
        } else if (node instanceof UnkeyedListEntryNode n) {
            writer.startUnkeyedListItem(n.name(), n.size());
            return writeChildren(n.body());
        } else if (node instanceof ChoiceNode n) {
            writer.startChoiceNode(n.name(), n.size());
            return writeChildren(n.body());
        } else if (node instanceof UnkeyedListNode n) {
            writer.startUnkeyedList(n.name(), n.size());
            return writeChildren(n.body());
        } else if (node instanceof UserMapNode n) {
            writer.startOrderedMapNode(n.name(), n.size());
            return writeChildren(n.body());
        } else if (node instanceof SystemMapNode n) {
            writer.startMapNode(n.name(), n.size());
            return writeChildren(n.body());
        } else if (node instanceof UserLeafSetNode<?> n) {
            writer.startOrderedLeafSet(n.name(), n.size());
            return writeChildren(n.body());
        } else if (node instanceof SystemLeafSetNode<?> n) {
            writer.startLeafSet(n.name(), n.size());
            return writeChildren(n.body());
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
            nnWriter.startMapEntryNode(node.name(), node.size());

            final Set<QName> qnames = node.name().keySet();
            // Write out all the key children
            for (final QName qname : qnames) {
                final DataContainerChild child = node.childByArg(new NodeIdentifier(qname));
                if (child != null) {
                    write(child);
                } else {
                    LOG.info("No child for key element {} found", qname);
                }
            }

            // Write all the rest
            return writeChildren(Iterables.filter(node.body(), input -> {
                if (qnames.contains(input.name().getNodeType())) {
                    LOG.debug("Skipping key child {}", input);
                    return false;
                }
                return true;
            }));
        }
    }
}
