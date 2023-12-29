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

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.builder.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.spi.node.InterningLeafNodeBuilder;
import org.opendaylight.yangtools.yang.data.spi.node.InterningLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.LeafInterner;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

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
public class ImmutableNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    private static final Interner<LeafSetEntryNode<?>> ENTRY_INTERNER = Interners.newWeakInterner();
    private static final BuilderFactory BUILDER_FACTORY = ImmutableNodes.builderFactory();

    private final Deque<NormalizedNode.Builder> builders = new ArrayDeque<>();

    private DataSchemaNode nextSchema;

    protected ImmutableNormalizedNodeStreamWriter(final NormalizedNode.Builder topLevelBuilder) {
        builders.push(topLevelBuilder);
    }

    protected ImmutableNormalizedNodeStreamWriter(final NormalizationResultHolder holder) {
        this(new NormalizationResultBuilder(holder));
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
     * @param holder {@link NormalizationResultHolder} object which will hold result value.
     * @return {@link NormalizedNodeStreamWriter} which will write item to supplied result holder.
     */
    public static @NonNull NormalizedNodeStreamWriter from(final NormalizationResultHolder holder) {
        return new ImmutableMetadataNormalizedNodeStreamWriter(holder);
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
        final var builder = UNKNOWN_SIZE == childSizeHint ? BUILDER_FACTORY.newSystemLeafSetBuilder()
            : BUILDER_FACTORY.newSystemLeafSetBuilder(childSizeHint);
        enter(name, leafSetNodeBuilder(builder, nextSchema));
    }

    private static <T> SystemLeafSetNode.Builder<T> leafSetNodeBuilder(final SystemLeafSetNode.Builder<T> delegate,
            final @Nullable DataSchemaNode schema) {
        if (schema instanceof LeafListSchemaNode leafListSchema) {
            final var type = leafListSchema.getType();
            if (type instanceof BooleanTypeDefinition || type instanceof EnumTypeDefinition
                    || type instanceof IdentityrefTypeDefinition) {
                return new InterningLeafSetNodeBuilder<T>(delegate, (Interner) ENTRY_INTERNER);
            }
        }
        return delegate;
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) {
        final var current = current();
        checkArgument(current instanceof LeafSetNode.Builder || current instanceof NormalizationResultBuilder,
            "LeafSetEntryNode is not valid for parent %s", current);
        enter(name, leafsetEntryNodeBuilder());
        nextSchema = null;
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? BUILDER_FACTORY.newUserLeafSetBuilder()
            : BUILDER_FACTORY.newUserLeafSetBuilder(childSizeHint));
    }

    @Override
    public boolean startAnyxmlNode(final NodeIdentifier name, final Class<?> objectModel) {
        checkDataNodeContainer();
        if (DOMSource.class.isAssignableFrom(objectModel)) {
            enter(name, BUILDER_FACTORY.newAnyxmlBuilder(DOMSource.class));
            nextSchema = null;
            return true;
        }
        return false;
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? BUILDER_FACTORY.newContainerBuilder()
            : BUILDER_FACTORY.newContainerBuilder(childSizeHint));
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? BUILDER_FACTORY.newUnkeyedListBuilder()
            : BUILDER_FACTORY.newUnkeyedListBuilder(childSizeHint));
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) {
        final var current = current();
        checkArgument(current instanceof UnkeyedListNode.Builder || current instanceof NormalizationResultBuilder);
        enter(name, UNKNOWN_SIZE == childSizeHint ? BUILDER_FACTORY.newUnkeyedListEntryBuilder()
            : BUILDER_FACTORY.newUnkeyedListEntryBuilder(childSizeHint));
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? BUILDER_FACTORY.newSystemMapBuilder()
            : BUILDER_FACTORY.newSystemMapBuilder(childSizeHint));
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint) {
        final var current = current();
        checkArgument(current instanceof MapNode.Builder || current instanceof NormalizationResultBuilder);

        enter(identifier, UNKNOWN_SIZE == childSizeHint ? BUILDER_FACTORY.newMapEntryBuilder()
            : BUILDER_FACTORY.newMapEntryBuilder(childSizeHint));
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? BUILDER_FACTORY.newUserMapBuilder()
            : BUILDER_FACTORY.newUserMapBuilder(childSizeHint));
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) {
        checkDataNodeContainer();
        enter(name, UNKNOWN_SIZE == childSizeHint ? BUILDER_FACTORY.newChoiceBuilder()
            : BUILDER_FACTORY.newChoiceBuilder(childSizeHint));
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
        // FIXME: tighten to concrete NormalizedNode.Builder interfaces
        currentScalar().withValue(value);
    }

    @Override
    public void domSourceValue(final DOMSource value) {
        // FIXME: tighten to concrete NormalizedNode.Builder interfaces
        currentScalar().withValue(value);
    }

    @Override
    public void endNode() {
        final var finishedBuilder = builders.poll();
        checkState(finishedBuilder != null, "Node which should be closed does not exists.");
        final var product = finishedBuilder.build();
        nextSchema = null;

        writeChild(product);
    }

    @Override
    public boolean startAnydataNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        checkDataNodeContainer();
        enter(name, BUILDER_FACTORY.newAnydataBuilder(objectModel));
        // We support all object model
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
    protected final void writeChild(final NormalizedNode child) {
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
    protected final NormalizedNode.Builder popBuilder() {
        return builders.pop();
    }

    final void reset(final NormalizationResultBuilder builder) {
        nextSchema = null;
        builders.clear();
        builders.push(builder);
    }

    private <T> LeafNode.Builder<T> leafNodeBuilder(final DataSchemaNode schema) {
        final var builder = this.<T>leafNodeBuilder();
        if (schema instanceof LeafSchemaNode leafSchema) {
            final var interner = LeafInterner.<LeafNode<T>>forSchema(leafSchema);
            if (interner.isPresent()) {
                return new InterningLeafNodeBuilder<>(builder, interner.orElseThrow());
            }
        }
        return builder;
    }

    <T> LeafNode.@NonNull Builder<T> leafNodeBuilder() {
        return BUILDER_FACTORY.newLeafBuilder();
    }

    <T> LeafSetEntryNode.@NonNull Builder<T> leafsetEntryNodeBuilder() {
        return BUILDER_FACTORY.newLeafSetEntryBuilder();
    }

    private void checkDataNodeContainer() {
        @SuppressWarnings("rawtypes")
        final NormalizedNodeContainerBuilder current = currentContainer();
        if (!(current instanceof NormalizationResultBuilder)) {
            checkArgument(current instanceof DataContainerNodeBuilder<?, ?>, "Invalid nesting of data.");
        }
    }

    private NormalizedNode.Builder current() {
        return builders.peek();
    }

    @SuppressWarnings("rawtypes")
    private NormalizedNodeContainerBuilder currentContainer() {
        final var current = current();
        if (current instanceof NormalizedNodeContainerBuilder builder) {
            return builder;
        }
        if (current != null) {
            throw new IllegalStateException(current + " is not a node container");
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private NormalizedNodeBuilder currentScalar() {
        final var current = current();
        if (current instanceof NormalizedNodeContainerBuilder) {
            throw new IllegalStateException("Unexpected node container " + current);
        }
        if (current instanceof NormalizedNodeBuilder builder) {
            return builder;
        }
        throw new IllegalStateException("Unexpected non-scalar " + current);
    }
}
