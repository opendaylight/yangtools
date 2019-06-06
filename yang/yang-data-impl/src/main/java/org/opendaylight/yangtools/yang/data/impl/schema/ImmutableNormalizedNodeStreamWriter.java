/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.AnydataExtension;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriterExtension;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnyXmlNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnydataNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableChoiceNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListEntryNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableYangModeledAnyXmlNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Implementation of {@link NormalizedNodeStreamWriter}, which constructs immutable instances of
 * {@link NormalizedNode}s.
 *
 * <p>
 * This writer supports two modes of behaviour one is using {@link #from(NormalizedNodeResult)} where resulting
 * NormalizedNode will be stored in supplied result object.
 *
 * <p>
 * Other mode of operation is using {@link #from(NormalizedNodeContainerBuilder)}, where all created nodes will be
 * written to this builder.
 *
 * <p>
 * This class is not final for purposes of customization, normal users should not need to subclass it.
 */
public class ImmutableNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter, AnydataExtension {
    @SuppressWarnings("rawtypes")
    private final Deque<NormalizedNodeBuilder> builders = new ArrayDeque<>();

    private DataSchemaNode nextSchema;

    @SuppressWarnings("rawtypes")
    protected ImmutableNormalizedNodeStreamWriter(final NormalizedNodeBuilder topLevelBuilder) {
        builders.push(topLevelBuilder);
    }

    protected ImmutableNormalizedNodeStreamWriter(final NormalizedNodeResult result) {
        this(new NormalizedNodeResultBuilder(result));
    }

    /**
     * Creates a {@link NormalizedNodeStreamWriter} which creates instances of supplied {@link NormalizedNode}s
     * and writes them to supplied builder as child nodes.
     *
     * <p>
     * Type of supplied {@link NormalizedNodeContainerBuilder} affects, which events could be emitted in order
     * to ensure proper construction of data.
     *
     * @param builder Builder to which data will be written.
     * @return {@link NormalizedNodeStreamWriter} which writes data
     */
    public static @NonNull NormalizedNodeStreamWriter from(final NormalizedNodeContainerBuilder<?, ?, ?, ?> builder) {
        return new ImmutableNormalizedNodeStreamWriter(builder);
    }

    /**
     * Creates a {@link NormalizedNodeStreamWriter} which creates one instance of top-level {@link NormalizedNode}
     * (type of NormalizedNode) is determined by first start event.
     *
     * <p>
     * Result is built when {@link #endNode()} associated with that start event is emitted.
     *
     * <p>
     * Writer properly creates also nested {@link NormalizedNode} instances, if their are supported inside the scope
     * of the first event.
     *
     * <p>
     * This method is useful for clients, which knows there will be one top-level node written, but does not know which
     * type of {@link NormalizedNode} will be written.
     *
     * @param result {@link NormalizedNodeResult} object which will hold result value.
     * @return {@link NormalizedNodeStreamWriter} which will write item to supplied result holder.
     */
    public static @NonNull NormalizedNodeStreamWriter from(final NormalizedNodeResult result) {
        return result instanceof NormalizedNodeMetadataResult ? from((NormalizedNodeMetadataResult) result)
                : new ImmutableNormalizedNodeStreamWriter(result);
    }

    /**
     * Creates a {@link NormalizedNodeStreamWriter} which creates one instance of top-level {@link NormalizedNode}
     * (type of NormalizedNode) is determined by first start event.
     *
     * <p>
     * Result is built when {@link #endNode()} associated with that start event is emitted.
     *
     * <p>
     * Writer properly creates also nested {@link NormalizedNode} instances, if their are supported inside the scope
     * of the first event.
     *
     * <p>
     * This method is useful for clients, which knows there will be one top-level node written, but does not know which
     * type of {@link NormalizedNode} will be written.
     *
     * @param result {@link NormalizedNodeResult} object which will hold result value.
     * @return {@link NormalizedNodeStreamWriter} which will write item to supplied result holder.
     */
    public static @NonNull NormalizedNodeStreamWriter from(final NormalizedNodeMetadataResult result) {
        return new ImmutableMetadataNormalizedNodeStreamWriter(result);
    }

    @Override
    public ClassToInstanceMap<NormalizedNodeStreamWriterExtension> getExtensions() {
        return ImmutableClassToInstanceMap.of(AnydataExtension.class, this);
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) {
        checkDataNodeContainer();
        enter(name, leafNodeBuilder(nextSchema));
        nextSchema = null;
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? InterningLeafSetNodeBuilder.create(nextSchema)
                : InterningLeafSetNodeBuilder.create(nextSchema, childSizeHint));
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) {
        final NormalizedNodeBuilder<?, ?, ?> current = current();
        checkArgument(current instanceof ImmutableLeafSetNodeBuilder
            || current instanceof ImmutableOrderedLeafSetNodeBuilder || current instanceof NormalizedNodeResultBuilder,
            "LeafSetEntryNode is not valid for parent %s", current);
        enter(name, leafsetEntryNodeBuilder());
        nextSchema = null;
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, Builders.orderedLeafSetBuilder());
    }

    @Override
    public void startAnyxmlNode(final NodeIdentifier name) {
        checkDataNodeContainer();
        enter(name, ImmutableAnyXmlNodeBuilder.create());
        nextSchema = null;
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? ImmutableContainerNodeBuilder.create()
                : ImmutableContainerNodeBuilder.create(childSizeHint));
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();

        checkArgument(nextSchema instanceof YangModeledAnyXmlSchemaNode,
                "Schema of this node should be instance of YangModeledAnyXmlSchemaNode");
        final YangModeledAnyXmlSchemaNode anyxmlSchema = (YangModeledAnyXmlSchemaNode) nextSchema;
        enter(name, UNKNOWN_SIZE == childSizeHint ? ImmutableYangModeledAnyXmlNodeBuilder.create(anyxmlSchema)
                : ImmutableYangModeledAnyXmlNodeBuilder.create(anyxmlSchema, childSizeHint));
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? ImmutableUnkeyedListNodeBuilder.create()
                : ImmutableUnkeyedListNodeBuilder.create(childSizeHint));
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) {
        final NormalizedNodeBuilder<?, ?, ?> current = current();
        checkArgument(current instanceof ImmutableUnkeyedListNodeBuilder
            || current instanceof NormalizedNodeResultBuilder);
        enter(name, UNKNOWN_SIZE == childSizeHint ? ImmutableUnkeyedListEntryNodeBuilder.create()
                : ImmutableUnkeyedListEntryNodeBuilder.create(childSizeHint));
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? ImmutableMapNodeBuilder.create()
                : ImmutableMapNodeBuilder.create(childSizeHint));
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint) {
        final NormalizedNodeBuilder<?, ?, ?> current = current();
        checkArgument(current instanceof ImmutableMapNodeBuilder || current instanceof ImmutableOrderedMapNodeBuilder
            || current instanceof NormalizedNodeResultBuilder);

        enter(identifier, UNKNOWN_SIZE == childSizeHint ? ImmutableMapEntryNodeBuilder.create()
                : ImmutableMapEntryNodeBuilder.create(childSizeHint));
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? ImmutableOrderedMapNodeBuilder.create()
                : ImmutableOrderedMapNodeBuilder.create(childSizeHint));
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? ImmutableChoiceNodeBuilder.create()
                : ImmutableChoiceNodeBuilder.create(childSizeHint));
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) {
        checkDataNodeContainer();
        checkArgument(!(current() instanceof ImmutableAugmentationNodeBuilder));
        enter(identifier, Builders.augmentationBuilder());
    }

    @Override
    public void flush() {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public void nextDataSchemaNode(final DataSchemaNode schema) {
        nextSchema = requireNonNull(schema);
    }

    @Override
    public void scalarValue(final Object value) {
        currentScalar().withValue(value);
    }

    @Override
    public void domSourceValue(final DOMSource value) {
        currentScalar().withValue(value);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void endNode() {
        final NormalizedNodeBuilder finishedBuilder = builders.poll();
        checkState(finishedBuilder != null, "Node which should be closed does not exists.");
        final NormalizedNode<PathArgument, ?> product = finishedBuilder.build();
        nextSchema = null;

        writeChild(product);
    }

    @Override
    public boolean startAnydataNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        checkDataNodeContainer();
        enter(name, ImmutableAnydataNodeBuilder.create(objectModel));
        // We support all object models
        return true;
    }

    /**
     * Add a child not to the currently-open builder.
     *
     * @param child A new child
     * @throws NullPointerException if {@code child} is null
     * @throws IllegalStateException if there is no open builder
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected final void writeChild(final NormalizedNode<?, ?> child) {
        final NormalizedNodeContainerBuilder current = currentContainer();
        checkState(current != null, "Reached top level node, which could not be closed in this writer.");
        current.addChild(requireNonNull(child));
    }

    // Exposed for ImmutableMetadataNormalizedNodeStreamWriter
    @SuppressWarnings("rawtypes")
    void enter(final PathArgument identifier, final NormalizedNodeBuilder next) {
        builders.push(next.withNodeIdentifier(identifier));
        nextSchema = null;
    }

    // Exposed for ImmutableMetadataNormalizedNodeStreamWriter
    protected final NormalizedNodeBuilder popBuilder() {
        return builders.pop();
    }

    final void reset(final NormalizedNodeResultBuilder builder) {
        nextSchema = null;
        builders.clear();
        builders.push(builder);
    }

    private <T> ImmutableLeafNodeBuilder<T> leafNodeBuilder(final DataSchemaNode schema) {
        final InterningLeafNodeBuilder<T> interning = InterningLeafNodeBuilder.forSchema(schema);
        return interning != null ? interning : leafNodeBuilder();
    }

    <T> ImmutableLeafNodeBuilder<T> leafNodeBuilder() {
        return new ImmutableLeafNodeBuilder<>();
    }

    <T> ImmutableLeafSetEntryNodeBuilder<T> leafsetEntryNodeBuilder() {
        return ImmutableLeafSetEntryNodeBuilder.create();
    }

    private void checkDataNodeContainer() {
        @SuppressWarnings("rawtypes")
        final NormalizedNodeContainerBuilder current = currentContainer();
        if (!(current instanceof NormalizedNodeResultBuilder)) {
            checkArgument(current instanceof DataContainerNodeBuilder<?, ?>, "Invalid nesting of data.");
        }
    }

    @SuppressWarnings("rawtypes")
    private NormalizedNodeBuilder current() {
        return builders.peek();
    }

    @SuppressWarnings("rawtypes")
    private NormalizedNodeContainerBuilder currentContainer() {
        final NormalizedNodeBuilder current = current();
        if (current == null) {
            return null;
        }
        checkState(current instanceof NormalizedNodeContainerBuilder, "%s is not a node container", current);
        return (NormalizedNodeContainerBuilder) current;
    }

    @SuppressWarnings("rawtypes")
    private NormalizedNodeBuilder currentScalar() {
        final NormalizedNodeBuilder current = current();
        checkState(!(current instanceof NormalizedNodeContainerBuilder), "Unexpected node container %s", current);
        return current;
    }
}
