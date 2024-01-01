/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2021 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.LeafrefResolver;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for tracking schema state underlying a {@link NormalizedNode} structure.
 */
@Beta
public final class NormalizedNodeStreamWriterStack implements LeafrefResolver {
    private static final Logger LOG = LoggerFactory.getLogger(NormalizedNodeStreamWriterStack.class);

    private final Deque<DataSchemaNode> schemaStack = new ArrayDeque<>();
    private final SchemaInferenceStack dataTree;
    private final DataNodeContainer root;

    private NormalizedNodeStreamWriterStack(final EffectiveModelContext context) {
        dataTree = SchemaInferenceStack.of(context);
        root = requireNonNull(context);
    }

    private NormalizedNodeStreamWriterStack(final SchemaInferenceStack dataTree) {
        this.dataTree = requireNonNull(dataTree);
        if (!dataTree.isEmpty()) {
            final var current = dataTree.currentStatement();
            if (current instanceof DataNodeContainer container) {
                root = container;
            } else {
                throw new IllegalArgumentException("Cannot instantiate on " + current);
            }
        } else {
            root = dataTree.modelContext();
        }
    }

    /**
     * Create a new writer with the specified inference state as its root.
     *
     * @param root Root inference state
     * @return A new {@link NormalizedNodeStreamWriter}
     * @throws NullPointerException if {@code root} is null
     */
    public static @NonNull NormalizedNodeStreamWriterStack of(final EffectiveStatementInference root) {
        return new NormalizedNodeStreamWriterStack(SchemaInferenceStack.ofInference(root));
    }

    /**
     * Create a new writer with the specified inference state as its root.
     *
     * @param root Root inference state
     * @return A new {@link NormalizedNodeStreamWriter}
     * @throws NullPointerException if {@code root} is null
     */
    public static @NonNull NormalizedNodeStreamWriterStack of(final Inference root) {
        return new NormalizedNodeStreamWriterStack(root.toSchemaInferenceStack());
    }

    /**
     * Create a new writer at the root of specified {@link EffectiveModelContext}.
     *
     * @param context effective model context
     * @return A new {@link NormalizedNodeStreamWriter}
     * @throws NullPointerException if {@code context} is null
     */
    public static @NonNull NormalizedNodeStreamWriterStack of(final EffectiveModelContext context) {
        return new NormalizedNodeStreamWriterStack(context);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified schema path.
     *
     * @param context Associated {@link EffectiveModelContext}
     * @param path schema path
     * @return A new {@link NormalizedNodeStreamWriterStack}
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if {@code path} does not point to a valid root
     */
    public static @NonNull NormalizedNodeStreamWriterStack of(final EffectiveModelContext context,
            final Absolute path) {
        return new NormalizedNodeStreamWriterStack(SchemaInferenceStack.of(context, path));
    }

    /**
     * Create a new writer with the specified context and rooted in the specified {@link YangInstanceIdentifier}..
     *
     * @param context Associated {@link EffectiveModelContext}
     * @param path Normalized path
     * @return A new {@link NormalizedNodeStreamWriterStack}
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if {@code path} does not point to a valid root
     */
    public static @NonNull NormalizedNodeStreamWriterStack of(final EffectiveModelContext context,
            final YangInstanceIdentifier path) {
        return new NormalizedNodeStreamWriterStack(DataSchemaContextTree.from(context)
            .enterPath(path)
            .orElseThrow()
            .stack());
    }

    /**
     * Create a new writer with the specified context and rooted in the specified schema path.
     *
     * @param context Associated {@link EffectiveModelContext}
     * @param operation Operation schema path
     * @param qname Input/Output container QName
     * @return A new {@link NormalizedNodeStreamWriter}
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if {@code operation} does not point to an actual operation or if {@code qname}
     *                                  does not identify a valid root underneath it.
     */
    public static @NonNull NormalizedNodeStreamWriterStack ofOperation(final EffectiveModelContext context,
            final Absolute operation, final QName qname) {
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context, operation);
        final EffectiveStatement<?, ?> current = stack.currentStatement();
        checkArgument(current instanceof RpcEffectiveStatement || current instanceof ActionEffectiveStatement,
            "Path %s resolved into non-operation %s", operation, current);
        stack.enterSchemaTree(qname);
        return new NormalizedNodeStreamWriterStack(stack);
    }

    @Override
    public TypeDefinition<?> resolveLeafref(final LeafrefTypeDefinition type) {
        return dataTree.resolveLeafref(type);
    }

    public Object getParent() {
        final var schema = schemaStack.peek();
        return schema == null ? root : schema;
    }

    private @NonNull SchemaNode enterDataTree(final PathArgument name) {
        final var qname = name.getNodeType();
        final var stmt = dataTree.enterDataTree(qname);
        if (!(stmt instanceof SchemaNode ret)) {
            throw new VerifyException("Unexpected result " + stmt);
        }
        if (getParent() instanceof ChoiceSchemaNode choice) {
            final var check = choice.findDataSchemaChild(qname).orElse(null);
            verify(check == ret, "Data tree result %s does not match choice result %s", ret, check);
        }
        return ret;
    }

    private <T extends DataSchemaNode> @NonNull T enterDataTree(final PathArgument name,
            final @NonNull Class<T> expectedClass, final @NonNull String humanString) {
        final var schema = enterDataTree(name);
        final T casted;
        try {
            casted = expectedClass.cast(schema);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Node " + schema + " is not " + humanString, e);
        }
        schemaStack.push(casted);
        return casted;
    }

    public void startList(final PathArgument name) {
        enterDataTree(name, ListSchemaNode.class, "a list");
    }

    public void startListItem(final PathArgument name) throws IOException {
        if (!(getParent() instanceof ListSchemaNode parentList)) {
            throw new IllegalArgumentException("List item is not appropriate");
        }
        schemaStack.push(parentList);
    }

    public void startLeafNode(final NodeIdentifier name) throws IOException {
        enterDataTree(name, LeafSchemaNode.class, "a leaf");
    }

    public void startLeafSet(final NodeIdentifier name) {
        enterDataTree(name, LeafListSchemaNode.class, "a leaf-list");
    }

    private @NonNull LeafListSchemaNode leafSetEntryNode(final QName qname) {
        final Object parent = getParent();
        if (parent instanceof LeafListSchemaNode leafList) {
            return leafList;
        }
        if (parent instanceof DataNodeContainer parentContainer) {
            final var child = parentContainer.dataChildByName(qname);
            if (child instanceof LeafListSchemaNode childLeafList) {
                return childLeafList;
            }
            throw new IllegalArgumentException(
                "Node " + child + " is neither a leaf-list nor currently in a leaf-list");
        }
        throw new IllegalArgumentException("Cannot lookup " + qname + " in parent " + parent);
    }

    public void startLeafSetEntryNode(final NodeWithValue<?> name) {
        schemaStack.push(leafSetEntryNode(name.getNodeType()));
    }

    public void startChoiceNode(final NodeIdentifier name) {
        LOG.debug("Enter choice {}", name);
        final var stmt = dataTree.enterChoice(name.getNodeType());
        if (stmt instanceof ChoiceSchemaNode choice) {
            schemaStack.push(choice);
        } else {
            throw new VerifyException("Node " + stmt + " is not a choice");
        }
    }

    public @NonNull ContainerLike startContainerNode(final NodeIdentifier name) {
        LOG.debug("Enter container {}", name);

        final ContainerLike schema;
        if (schemaStack.isEmpty() && root instanceof NotificationDefinition notification
            && name.getNodeType().equals(notification.getQName())) {
            // Special case for stacks initialized at notification. We pretend the first container is contained within
            // itself.
            // FIXME: 8.0.0: factor this special case out to something more reasonable, like being initialized at the
            //               Notification's parent and knowing to enterSchemaTree() instead of enterDataTree().
            schema = notification.toContainerLike();
            schemaStack.push(schema);
        } else {
            schema = enterDataTree(name, ContainerLike.class, "a container");
        }

        return schema;
    }

    public void startAnyxmlNode(final NodeIdentifier name) {
        enterDataTree(name, AnyxmlSchemaNode.class, "anyxml");
    }

    public void startAnydataNode(final NodeIdentifier name) {
        enterDataTree(name, AnydataSchemaNode.class, "anydata");
    }

    public Object endNode() {
        final var ret = schemaStack.pop();
        // If this is a data tree node, make sure it is updated. Before that, though, we need to check if this is not
        // actually listEntry -> list or leafListEntry -> leafList exit.
        if (getParent() != ret) {
            dataTree.exit();
        }
        return ret;
    }
}
