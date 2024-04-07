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
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.schema.AnydataNode;
import org.opendaylight.yangtools.yang.data.api.schema.AnyxmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
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
@NonNullByDefault
public class NormalizedNodeWriter implements Closeable, Flushable {
    private static final Logger LOG = LoggerFactory.getLogger(NormalizedNodeWriter.class);

    protected final NormalizedNodeStreamWriter writer;
    private final MapBodyOrder mapBodyOrder;

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}.
     *
     * @param writer Back-end writer
     */
    public NormalizedNodeWriter(final NormalizedNodeStreamWriter writer) {
        this(writer, false);
    }

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}, optionally using iteration order to emit
     * {@link MapEntryNode} bodies.
     *
     * @param writer Back-end writer
     */
    public NormalizedNodeWriter(final NormalizedNodeStreamWriter writer, final boolean iterationOrder) {
        this.writer = requireNonNull(writer);
        mapBodyOrder = iterationOrder ? IterationMapBodyOrder.INSTANCE : DefaultMapBodyOrder.INSTANCE;
    }

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}.
     *
     * @param writer Back-end writer
     * @return A new instance.
     * @deprecated Use {@link #NormalizedNodeWriter(NormalizedNodeStreamWriter)} instead.
     */
    @Deprecated(forRemoval = true, since = "13.0.3")
    public static NormalizedNodeWriter forStreamWriter(final NormalizedNodeStreamWriter writer) {
        return new NormalizedNodeWriter(writer);
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
     * @deprecated Use {@link #NormalizedNodeWriter(NormalizedNodeStreamWriter, boolean)} instead. Note the boolean's
     *             meaning is inverted.
     */
    @Deprecated(forRemoval = true, since = "13.0.3")
    public static NormalizedNodeWriter forStreamWriter(final NormalizedNodeStreamWriter writer,
            final boolean orderKeyLeaves) {
        return new NormalizedNodeWriter(writer, !orderKeyLeaves);
    }

    @Override
    public final void flush() throws IOException {
        writer.flush();
    }

    @Override
    public final void close() throws IOException {
        writer.flush();
        writer.close();
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
        if (node instanceof ContainerNode n) {
            writeContainer(n);
        } else if (node instanceof MapEntryNode n) {
            writeMapEntry(n);
        } else if (node instanceof UnkeyedListEntryNode n) {
            writeUnkeyedListEntry(n);
        } else if (node instanceof ChoiceNode n) {
            writeChoice(n);
        } else if (node instanceof UnkeyedListNode n) {
            writeUnkeyedList(n);
        } else if (node instanceof UserMapNode n) {
            writeUserMap(n);
        } else if (node instanceof SystemMapNode n) {
            writeSystemMap(n);
        } else if (node instanceof UserLeafSetNode<?> n) {
            writeUserLeafSet(n);
        } else if (node instanceof SystemLeafSetNode<?> n) {
            writeSystemLeafSet(n);
        } else if (node instanceof LeafSetEntryNode<?> n) {
            writeLeafSetEntry(n);
        } else if (node instanceof LeafNode<?> n) {
            writeLeaf(n);
        } else if (node instanceof AnyxmlNode<?> anyxmlNode) {
            writeAnyxml(anyxmlNode);
        } else if (node instanceof AnydataNode<?> n) {
            writeAnydata(n);
        } else {
            throw new IllegalStateException("Unhandled " + node.contract());
        }
        return this;
    }

    protected void writeAnydata(final AnydataNode<?> node) throws IOException {
        final var model = node.bodyObjectModel();
        if (writer.startAnydataNode(node.name(), model)) {
            writer.scalarValue(node.body());
            writer.endNode();
        } else {
            LOG.debug("Writer {} does not support anydata in form of {}", writer, model);
        }
    }

    protected void writeAnyxml(final AnyxmlNode<?> node) throws IOException {
        final Class<?> model = node.bodyObjectModel();
        if (writer.startAnyxmlNode(node.name(), model)) {
            final Object value = node.body();
            if (DOMSource.class.isAssignableFrom(model)) {
                verify(value instanceof DOMSource, "Inconsistent anyxml node %s", node);
                writer.domSourceValue((DOMSource) value);
            } else {
                writer.scalarValue(value);
            }
            writer.endNode();
        } else {
            LOG.debug("Ignoring unhandled anyxml node {}", node);
        }
    }

    protected void writeChoice(final ChoiceNode node) throws IOException {
        writer.startChoiceNode(node.name(), node.size());
        writeChildren(node.body());
    }

    protected void writeContainer(final ContainerNode node) throws IOException {
        writer.startContainerNode(node.name(), node.size());
        writeChildren(node.body());
    }

    protected void writeLeaf(final LeafNode<?> node) throws IOException {
        writer.startLeafNode(node.name());
        writer.scalarValue(node.body());
        writer.endNode();
    }

    protected void writeLeafSetEntry(final LeafSetEntryNode<?> node) throws IOException {
        writer.startLeafSetEntryNode(node.name());
        writer.scalarValue(node.body());
        writer.endNode();
    }

    protected void writeMapEntry(final MapEntryNode node) throws IOException {
        writer.startMapEntryNode(node.name(), node.size());
        writeChildren(mapBodyOrder.orderBody(node));
    }

    protected void writeSystemMap(final SystemMapNode node) throws IOException {
        writer.startMapNode(node.name(), node.size());
        writeChildren(node.body());
    }

    protected void writeUnkeyedList(final UnkeyedListNode node) throws IOException {
        writer.startUnkeyedList(node.name(), node.size());
        writeChildren(node.body());
    }

    protected void writeUnkeyedListEntry(final UnkeyedListEntryNode node) throws IOException {
        writer.startUnkeyedListItem(node.name(), node.size());
        writeChildren(node.body());
    }

    protected void writeUserLeafSet(final UserLeafSetNode<?> node) throws IOException {
        writer.startOrderedLeafSet(node.name(), node.size());
        writeChildren(node.body());
    }

    protected void writeSystemLeafSet(final SystemLeafSetNode<?> node) throws IOException {
        writer.startLeafSet(node.name(), node.size());
        writeChildren(node.body());
    }

    protected void writeUserMap(final UserMapNode node) throws IOException {
        writer.startOrderedMapNode(node.name(), node.size());
        writeChildren(node.body());
    }

    /**
     * Emit events for all children and then emit an endNode() event.
     *
     * @param children Child iterable
     * @throws IOException when the writer reports it
     */
    protected void writeChildren(final Iterable<? extends NormalizedNode> children) throws IOException {
        for (var child : children) {
            write(child);
        }
        writer.endNode();
    }
}
