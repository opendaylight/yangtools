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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAnyXmlNodeBuilder;
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
 */
public class ImmutableNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {

    @SuppressWarnings("rawtypes")
    private final Deque<NormalizedNodeBuilder> builders = new ArrayDeque<>();
    private DataSchemaNode nextSchema;

    @SuppressWarnings("rawtypes")
    protected ImmutableNormalizedNodeStreamWriter(final NormalizedNodeContainerBuilder topLevelBuilder) {
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
    public static NormalizedNodeStreamWriter from(final NormalizedNodeResult result) {
        return new ImmutableNormalizedNodeStreamWriter(result);
    }

    protected Deque<NormalizedNodeBuilder> getBuilders() {
        return builders;
    }

    @SuppressWarnings("rawtypes")
    protected NormalizedNodeBuilder getCurrent() {
        return builders.peek();
    }

    @SuppressWarnings("rawtypes")
    protected NormalizedNodeContainerBuilder getCurrentContainer() {
        final NormalizedNodeBuilder current = getCurrent();
        if (current == null) {
            return null;
        }
        checkState(current instanceof NormalizedNodeContainerBuilder, "%s is not a node container", current);
        return (NormalizedNodeContainerBuilder) current;
    }

    @SuppressWarnings("rawtypes")
    protected NormalizedNodeBuilder getCurrentScalar() {
        final NormalizedNodeBuilder current = getCurrent();
        checkState(!(current instanceof NormalizedNodeContainerBuilder), "Unexpected node container %s", current);
        return current;
    }

    @SuppressWarnings("rawtypes")
    private void enter(final NormalizedNodeBuilder next) {
        builders.push(next);
        nextSchema = null;
    }

    @SuppressWarnings("unchecked")
    protected void writeChild(final NormalizedNode<?, ?> child) {
        getCurrentContainer().addChild(child);
    }

    @Override
    @SuppressWarnings({"rawtypes","unchecked"})
    public void endNode() {
        final NormalizedNodeBuilder finishedBuilder = builders.poll();
        checkState(finishedBuilder != null, "Node which should be closed does not exists.");
        final NormalizedNodeContainerBuilder current = getCurrentContainer();
        checkState(current != null, "Reached top level node, which could not be closed in this writer.");
        final NormalizedNode<PathArgument, ?> product = finishedBuilder.build();
        current.addChild(product);
        nextSchema = null;
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) {
        checkDataNodeContainer();

        // FIXME: add interning
        //      if (nextSchema instanceof LeafSchemaNode) {
        //      node = LeafInterner.forSchema((LeafSchemaNode)nextSchema).intern(sample);
        enter(ImmutableLeafNodeBuilder.create().withNodeIdentifier(name));
        nextSchema = null;
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        final ListNodeBuilder<Object, LeafSetEntryNode<Object>> builder = UNKNOWN_SIZE == childSizeHint
                ? InterningLeafSetNodeBuilder.create(nextSchema)
                        : InterningLeafSetNodeBuilder.create(nextSchema, childSizeHint);
        builder.withNodeIdentifier(name);
        enter(builder);
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) {
        @SuppressWarnings("rawtypes")
        final NormalizedNodeBuilder current = getCurrent();
        checkArgument(current instanceof ImmutableOrderedLeafSetNodeBuilder
            || current instanceof ImmutableLeafSetNodeBuilder, "LeafSetEntryNode is not valid for parent %s", current);
        enter(ImmutableLeafSetEntryNodeBuilder.create().withNodeIdentifier(name));
        nextSchema = null;
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        final ListNodeBuilder<Object, LeafSetEntryNode<Object>> builder = Builders.orderedLeafSetBuilder();
        builder.withNodeIdentifier(name);
        enter(builder);
    }

    @Override
    public void startAnyxmlNode(final NodeIdentifier name) {
        checkDataNodeContainer();
        enter(ImmutableAnyXmlNodeBuilder.create().withNodeIdentifier(name));
        nextSchema = null;
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();

        final DataContainerNodeAttrBuilder<NodeIdentifier, ContainerNode> builder =
                UNKNOWN_SIZE == childSizeHint ? ImmutableContainerNodeBuilder.create()
                        : ImmutableContainerNodeBuilder.create(childSizeHint);
        enter(builder.withNodeIdentifier(name));
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();

        checkArgument(nextSchema instanceof YangModeledAnyXmlSchemaNode,
                "Schema of this node should be instance of YangModeledAnyXmlSchemaNode");
        final DataContainerNodeAttrBuilder<NodeIdentifier, YangModeledAnyXmlNode> builder =
                UNKNOWN_SIZE == childSizeHint
                ? ImmutableYangModeledAnyXmlNodeBuilder.create((YangModeledAnyXmlSchemaNode) nextSchema)
                        : ImmutableYangModeledAnyXmlNodeBuilder.create(
                (YangModeledAnyXmlSchemaNode) nextSchema, childSizeHint);
        enter(builder.withNodeIdentifier(name));
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();

        final CollectionNodeBuilder<UnkeyedListEntryNode, UnkeyedListNode> builder =
                UNKNOWN_SIZE == childSizeHint ? ImmutableUnkeyedListNodeBuilder.create()
                        : ImmutableUnkeyedListNodeBuilder.create(childSizeHint);
        enter(builder.withNodeIdentifier(name));
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) {
        checkArgument(getCurrent() instanceof NormalizedNodeResultBuilder
                || getCurrent() instanceof ImmutableUnkeyedListNodeBuilder);
        final DataContainerNodeAttrBuilder<NodeIdentifier, UnkeyedListEntryNode> builder =
                UNKNOWN_SIZE == childSizeHint ? ImmutableUnkeyedListEntryNodeBuilder.create()
                        : ImmutableUnkeyedListEntryNodeBuilder.create(childSizeHint);
        enter(builder.withNodeIdentifier(name));
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();

        final CollectionNodeBuilder<MapEntryNode, MapNode> builder = UNKNOWN_SIZE == childSizeHint
                ? ImmutableMapNodeBuilder.create() : ImmutableMapNodeBuilder.create(childSizeHint);
        enter(builder.withNodeIdentifier(name));
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint) {
        if (!(getCurrent() instanceof NormalizedNodeResultBuilder)) {
            checkArgument(getCurrent() instanceof ImmutableMapNodeBuilder
                || getCurrent() instanceof ImmutableOrderedMapNodeBuilder);
        }

        final DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> builder =
                UNKNOWN_SIZE == childSizeHint ? ImmutableMapEntryNodeBuilder.create()
                        : ImmutableMapEntryNodeBuilder.create(childSizeHint);
        enter(builder.withNodeIdentifier(identifier));
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();

        final CollectionNodeBuilder<MapEntryNode, OrderedMapNode> builder = UNKNOWN_SIZE == childSizeHint
                ? ImmutableOrderedMapNodeBuilder.create() : ImmutableOrderedMapNodeBuilder.create(childSizeHint);
        enter(builder.withNodeIdentifier(name));
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();

        final DataContainerNodeBuilder<NodeIdentifier, ChoiceNode> builder = UNKNOWN_SIZE == childSizeHint
                ? ImmutableChoiceNodeBuilder.create() : ImmutableChoiceNodeBuilder.create(childSizeHint);
        enter(builder.withNodeIdentifier(name));
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) {
        checkDataNodeContainer();
        checkArgument(!(getCurrent() instanceof ImmutableAugmentationNodeBuilder));
        enter(Builders.augmentationBuilder().withNodeIdentifier(identifier));
    }

    private void checkDataNodeContainer() {
        @SuppressWarnings("rawtypes")
        final NormalizedNodeContainerBuilder current = getCurrentContainer();
        if (!(current instanceof NormalizedNodeResultBuilder)) {
            checkArgument(current instanceof DataContainerNodeBuilder<?, ?>, "Invalid nesting of data.");
        }
    }

    @SuppressWarnings("rawtypes")
    protected static final class NormalizedNodeResultBuilder implements NormalizedNodeContainerBuilder {

        private final NormalizedNodeResult result;

        public NormalizedNodeResultBuilder(final NormalizedNodeResult result) {
            this.result = result;
        }

        @Override
        public NormalizedNodeBuilder withValue(final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public NormalizedNodeContainerBuilder withValue(final Collection value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public NormalizedNode build() {
            throw new IllegalStateException("Can not close NormalizedNodeResult");
        }

        @Override
        public NormalizedNodeContainerBuilder withNodeIdentifier(final PathArgument nodeIdentifier) {
            throw new UnsupportedOperationException();
        }

        @Override
        public NormalizedNodeContainerBuilder addChild(final NormalizedNode child) {
            result.setResult(child);
            return this;
        }

        @Override
        public NormalizedNodeContainerBuilder removeChild(final PathArgument key) {
            throw new UnsupportedOperationException();
        }
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
        getCurrentScalar().withValue(value);
    }
}
