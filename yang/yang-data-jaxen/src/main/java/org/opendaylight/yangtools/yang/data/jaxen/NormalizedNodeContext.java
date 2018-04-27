/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Context wrapper around a {@link NormalizedNode} for use with Jaxen. It tracks the parent node for purposes of
 * traversing upwards the NormalizedNode tree.
 */
@NonNullByDefault
final class NormalizedNodeContext extends Context implements Function<NormalizedNode<?, ?>, NormalizedNodeContext> {
    private static final long serialVersionUID = 1L;
    private final @Nullable NormalizedNodeContext parent;
    private final DataSchemaContextNode<?> schema;
    private final NormalizedNode<?, ?> node;

    private NormalizedNodeContext(final ContextSupport contextSupport, final DataSchemaContextNode<?> schema,
        final NormalizedNode<?, ?> node, final @Nullable NormalizedNodeContext parent) {
        super(contextSupport);
        this.schema = requireNonNull(schema);
        this.node = requireNonNull(node);
        this.parent = parent;

        setNodeSet(ImmutableList.of(this));
    }

    static NormalizedNodeContext forRoot(final NormalizedNodeContextSupport contextSupport) {
        final JaxenDocument document = contextSupport.getNavigator().getDocument();
        return new NormalizedNodeContext(contextSupport, document.getSchema(), document.getRootNode(), null);
    }

    NormalizedNode<?, ?> getNode() {
        return node;
    }

    @Nullable NormalizedNodeContext getParent() {
        return parent;
    }

    YangInstanceIdentifier getPath() {
        return (parent == null ? YangInstanceIdentifier.EMPTY : parent.getPath()).node(node.getIdentifier());
    }

    DataSchemaContextNode<?> getSchema() {
        return schema;
    }

    @Override
    public NormalizedNodeContext apply(final NormalizedNode<?, ?> input) {
        DataSchemaContextNode<?> childSchema = schema.getChild(input.getIdentifier());
        if (childSchema == null) {
            /* This feels very much like a hack: but solves lookup of child nodes with predicates.
             *
             * What is happening is that a Map context gets queried for its children, which results in contexts being
             * backed by UnorderedMapMixinContextNode, which requires us to unmask it.
             *
             * When the predicate is being evaluated, each child is queried for its child -- but since it is protected,
             * we cannot find it.
             */
            final DataSchemaNode mySchema = schema.getDataSchemaNode();
            if (mySchema instanceof ListSchemaNode) {
                childSchema = verifyNotNull(schema.getChild(mySchema.getQName())).getChild(input.getIdentifier());
            }
        }

        checkArgument(childSchema != null, "Failed to find schema for child %s", input);
        return new NormalizedNodeContext(getContextSupport(), childSchema, input, this);
    }
}
