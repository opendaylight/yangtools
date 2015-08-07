/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableAugmentationNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUnkeyedListNodeBuilder;

/**
 *
 * Implementation of {@link NormalizedNodeStreamWriter}, which constructs
 * immutable instances of {@link NormalizedNode}s.
 * <p>
 * This writer supports two modes of behaviour one is using {@link #from(NormalizedNodeResult)}
 * where resulting NormalizedNode will be stored in supplied result object.
 *
 * Other mode of operation is using {@link #from(NormalizedNodeContainerBuilder)},
 * where all created nodes will be written to this builder.
 *
 *
 */
public class ImmutableNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {

    @SuppressWarnings("rawtypes")
    private final Deque<NormalizedNodeContainerBuilder> builders = new ArrayDeque<>();

    @SuppressWarnings("rawtypes")
    private ImmutableNormalizedNodeStreamWriter( final NormalizedNodeContainerBuilder topLevelBuilder) {
        builders.push(topLevelBuilder);
    }

    /**
     * Creates a {@link NormalizedNodeStreamWriter} which creates instances of supplied
     * {@link NormalizedNode}s and writes them to supplied builder as child nodes.
     * <p>
     * Type of supplied {@link NormalizedNodeContainerBuilder} affects,
     * which events could be emitted in order to ensure proper construction of
     * data.
     *
     * @param builder Builder to which data will be written.
     * @return {@link NormalizedNodeStreamWriter} which writes data
     */
    public static final NormalizedNodeStreamWriter from(final NormalizedNodeContainerBuilder<?, ?, ?, ?> builder) {
        return new ImmutableNormalizedNodeStreamWriter(builder);
    }

    /**
     *
     * Creates a {@link NormalizedNodeStreamWriter} which creates one instance of top
     * level {@link NormalizedNode} (type of NormalizedNode) is determined by first
     * start event.
     * <p>
     * Result is built when {@link #endNode()} associated with that start event
     * is emitted.
     * <p>
     * Writer properly creates also nested {@link NormalizedNode} instances,
     * if their are supported inside the scope of first event.
     * <p>
     * This method is useful for clients, which knows there will be one
     * top level node written, but does not know which type of {@link NormalizedNode}
     * will be writen.
     *
     *
     * @param result {@link NormalizedNodeResult} object which will hold result value.
     * @return {@link NormalizedNodeStreamWriter} whcih will write item to supplied result holder.
     */
    public static final NormalizedNodeStreamWriter from(final NormalizedNodeResult result) {
        return new ImmutableNormalizedNodeStreamWriter(new NormalizedNodeResultBuilder(result));
    }


    @SuppressWarnings("rawtypes")
    private NormalizedNodeContainerBuilder getCurrent() {
        return builders.peek();
    }

    @SuppressWarnings("rawtypes")
    private void enter(final NormalizedNodeContainerBuilder next) {
        builders.push(next);
    }

    @SuppressWarnings("unchecked")
    private void writeChild(final NormalizedNode<?, ?> child) {
        getCurrent().addChild(child);
    }

    @Override
    @SuppressWarnings({"rawtypes","unchecked"})
    public void endNode() {
        final NormalizedNodeContainerBuilder finishedBuilder = builders.poll();
        Preconditions.checkState(finishedBuilder != null, "Node which should be closed does not exists.");
        NormalizedNodeContainerBuilder current = getCurrent();
        Preconditions.checkState(current != null, "Reached top level node, which could not be closed in this writer.");
        NormalizedNode<PathArgument, ?> product = finishedBuilder.build();
        current.addChild(product);
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IllegalArgumentException {
        checkDataNodeContainer();
        writeChild(ImmutableNodes.leafNode(name, value));
    }

    @Override
    public void startLeafSet(final NodeIdentifier name,final int childSizeHint) throws IllegalArgumentException {
        checkDataNodeContainer();
        ListNodeBuilder<Object, LeafSetEntryNode<Object>> builder = Builders.leafSetBuilder();
        builder.withNodeIdentifier(name);
        enter(builder);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void leafSetEntryNode(final Object value) throws IllegalArgumentException {
        Preconditions.checkArgument(getCurrent() instanceof ImmutableLeafSetNodeBuilder<?> || getCurrent() instanceof
                ImmutableOrderedLeafSetNodeBuilder<?>);
        ListNodeBuilder<Object, LeafSetEntryNode<Object>> builder;

        if (getCurrent() instanceof ImmutableOrderedLeafSetNodeBuilder<?>) {
            builder = ((ImmutableOrderedLeafSetNodeBuilder<Object>) getCurrent());
        } else {
            builder = ((ImmutableLeafSetNodeBuilder<Object>) getCurrent());
        }
        builder.withChildValue(value);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name,final int childSizeHint) throws IllegalArgumentException {
        checkDataNodeContainer();
        ListNodeBuilder<Object, LeafSetEntryNode<Object>> builder = Builders.orderedLeafSetBuilder();
        builder.withNodeIdentifier(name);
        enter(builder);
    }

    @Override
    public void anyxmlNode(final NodeIdentifier name, final Object value) throws IllegalArgumentException {
        checkDataNodeContainer();


    }

    @Override
    public void startContainerNode(final NodeIdentifier name,final int childSizeHint) throws IllegalArgumentException {
        checkDataNodeContainer();
        enter(Builders.containerBuilder().withNodeIdentifier(name));
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name,final int childSizeHint) throws IllegalArgumentException {
        checkDataNodeContainer();
        enter(Builders.unkeyedListBuilder().withNodeIdentifier(name));
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name,final int childSizeHint) throws IllegalStateException {
        Preconditions.checkArgument(getCurrent() instanceof ImmutableUnkeyedListNodeBuilder || getCurrent() instanceof NormalizedNodeResultBuilder);
        enter(Builders.unkeyedListEntryBuilder().withNodeIdentifier(name));
    }

    @Override
    public void startMapNode(final NodeIdentifier name,final int childSizeHint) throws IllegalArgumentException {
        checkDataNodeContainer();
        enter(Builders.mapBuilder().withNodeIdentifier(name));
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier,final int childSizeHint) throws IllegalArgumentException {
        if(!(getCurrent() instanceof NormalizedNodeResultBuilder)) {
            Preconditions.checkArgument(getCurrent() instanceof ImmutableMapNodeBuilder || getCurrent() instanceof ImmutableOrderedMapNodeBuilder);
        }
        enter(Builders.mapEntryBuilder().withNodeIdentifier(identifier));
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name,final int childSizeHint) throws IllegalArgumentException {
        checkDataNodeContainer();
        enter(Builders.orderedMapBuilder().withNodeIdentifier(name));
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name,final int childSizeHint) throws IllegalArgumentException {
        checkDataNodeContainer();
        enter(Builders.choiceBuilder().withNodeIdentifier(name));
    }
    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) throws IllegalArgumentException {
        checkDataNodeContainer();
        Preconditions.checkArgument(!(getCurrent() instanceof ImmutableAugmentationNodeBuilder));
        enter(Builders.augmentationBuilder().withNodeIdentifier(identifier));
    }

    private void checkDataNodeContainer() {
        @SuppressWarnings("rawtypes")
        NormalizedNodeContainerBuilder current = getCurrent();
        if(!(current instanceof NormalizedNodeResultBuilder)) {
        Preconditions.checkArgument(current instanceof DataContainerNodeBuilder<?, ?>, "Invalid nesting of data.");
        }
    }

    @SuppressWarnings("rawtypes")
    private static final class NormalizedNodeResultBuilder implements NormalizedNodeContainerBuilder {

        private final NormalizedNodeResult result;

        public NormalizedNodeResultBuilder(final NormalizedNodeResult result) {
            this.result = result;
        }

        @Override
        public NormalizedNodeBuilder withValue(final Object value) {
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
        public NormalizedNodeContainerBuilder withValue(final List value) {
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
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

}
