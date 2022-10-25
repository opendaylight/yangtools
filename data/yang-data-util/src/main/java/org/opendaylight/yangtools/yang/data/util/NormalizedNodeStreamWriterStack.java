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
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
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
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;
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

    private final Deque<WithStatus> schemaStack = new ArrayDeque<>();
    private final SchemaInferenceStack dataTree;
    private final DataNodeContainer root;

    private NormalizedNodeStreamWriterStack(final EffectiveModelContext context) {
        dataTree = SchemaInferenceStack.of(context);
        root = requireNonNull(context);
    }

    private NormalizedNodeStreamWriterStack(final SchemaInferenceStack dataTree) {
        this.dataTree = requireNonNull(dataTree);
        if (!dataTree.isEmpty()) {
            final EffectiveStatement<?, ?> current = dataTree.currentStatement();
            checkArgument(current instanceof DataNodeContainer, "Cannot instantiate on %s", current);

            root = (DataNodeContainer) current;
        } else {
            root = dataTree.getEffectiveModelContext();
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
        final WithStatus schema = schemaStack.peek();
        return schema == null ? root : schema;
    }

    private SchemaNode enterDataTree(final PathArgument name) {
        final QName qname = name.getNodeType();
        final DataTreeEffectiveStatement<?> stmt = dataTree.enterDataTree(qname);
        verify(stmt instanceof SchemaNode, "Unexpected result %s", stmt);
        final SchemaNode ret = (SchemaNode) stmt;
        final Object parent = getParent();
        if (parent instanceof ChoiceSchemaNode choice) {
            final DataSchemaNode check = choice.findDataSchemaChild(qname).orElse(null);
            verify(check == ret, "Data tree result %s does not match choice result %s", ret, check);
        }
        return ret;
    }

    public void startList(final PathArgument name) {
        final SchemaNode schema = enterDataTree(name);
        checkArgument(schema instanceof ListSchemaNode, "Node %s is not a list", schema);
        schemaStack.push(schema);
    }

    public void startListItem(final PathArgument name) throws IOException {
        final Object schema = getParent();
        checkArgument(schema instanceof ListSchemaNode, "List item is not appropriate");
        schemaStack.push((ListSchemaNode) schema);
    }

    public void startLeafNode(final NodeIdentifier name) throws IOException {
        final SchemaNode schema = enterDataTree(name);
        checkArgument(schema instanceof LeafSchemaNode, "Node %s is not a leaf", schema);
        schemaStack.push(schema);
    }

    public LeafListSchemaNode startLeafSet(final NodeIdentifier name) {
        final SchemaNode schema = enterDataTree(name);
        checkArgument(schema instanceof LeafListSchemaNode, "Node %s is not a leaf-list", schema);
        schemaStack.push(schema);
        return (LeafListSchemaNode) schema;
    }

    public LeafListSchemaNode leafSetEntryNode(final QName qname) {
        final Object parent = getParent();
        if (parent instanceof LeafListSchemaNode leafList) {
            return leafList;
        }
        checkArgument(parent instanceof DataNodeContainer, "Cannot lookup %s in parent %s", qname, parent);
        final DataSchemaNode child = ((DataNodeContainer) parent).dataChildByName(qname);
        checkArgument(child instanceof LeafListSchemaNode,
            "Node %s is neither a leaf-list nor currently in a leaf-list", child);
        return (LeafListSchemaNode) child;
    }

    public void startLeafSetEntryNode(final NodeWithValue<?> name) {
        schemaStack.push(leafSetEntryNode(name.getNodeType()));
    }

    public ChoiceSchemaNode startChoiceNode(final NodeIdentifier name) {
        LOG.debug("Enter choice {}", name);
        final ChoiceEffectiveStatement stmt = dataTree.enterChoice(name.getNodeType());
        verify(stmt instanceof ChoiceSchemaNode, "Node %s is not a choice", stmt);
        final ChoiceSchemaNode ret = (ChoiceSchemaNode) stmt;
        schemaStack.push(ret);
        return ret;
    }

    public SchemaNode startContainerNode(final NodeIdentifier name) {
        LOG.debug("Enter container {}", name);

        final SchemaNode schema;
        if (schemaStack.isEmpty() && root instanceof NotificationDefinition notification) {
            // Special case for stacks initialized at notification. We pretend the first container is contained within
            // itself.
            // FIXME: 8.0.0: factor this special case out to something more reasonable, like being initialized at the
            //               Notification's parent and knowing to enterSchemaTree() instead of enterDataTree().
            schema = notification;
        } else {
            schema = enterDataTree(name);
            checkArgument(schema instanceof ContainerLike, "Node %s is not a container", schema);
        }

        schemaStack.push(schema);
        return schema;
    }

    public void startAnyxmlNode(final NodeIdentifier name) {
        final SchemaNode schema = enterDataTree(name);
        checkArgument(schema instanceof AnyxmlSchemaNode, "Node %s is not anyxml", schema);
        schemaStack.push(schema);
    }

    public void startAnydataNode(final NodeIdentifier name) {
        final SchemaNode schema = enterDataTree(name);
        checkArgument(schema instanceof AnydataSchemaNode, "Node %s is not anydata", schema);
        schemaStack.push(schema);
    }

    public Object endNode() {
        final Object ret = schemaStack.pop();
        // If this is a data tree node, make sure it is updated. Before that, though, we need to check if this is not
        // actually listEntry -> list or leafListEntry -> leafList exit.
        if (!(ret instanceof AugmentationSchemaNode) && getParent() != ret) {
            dataTree.exit();
        }
        return ret;
    }

    public AugmentationSchemaNode startAugmentationNode(final AugmentationIdentifier identifier) {
        LOG.debug("Enter augmentation {}", identifier);
        Object parent = getParent();

        checkArgument(parent instanceof AugmentationTarget, "Augmentation not allowed under %s", parent);
        if (parent instanceof ChoiceSchemaNode choice) {
            final QName name = Iterables.get(identifier.getPossibleChildNames(), 0);
            parent = findCaseByChild(choice, name);
        }
        checkArgument(parent instanceof DataNodeContainer, "Augmentation allowed only in DataNodeContainer", parent);
        final AugmentationSchemaNode schema = findSchemaForAugment((AugmentationTarget) parent,
            identifier.getPossibleChildNames());
        final AugmentationSchemaNode resolvedSchema = new EffectiveAugmentationSchema(schema,
            (DataNodeContainer) parent);
        schemaStack.push(resolvedSchema);
        return resolvedSchema;
    }

    // FIXME: 7.0.0: can we get rid of this?
    private static @NonNull AugmentationSchemaNode findSchemaForAugment(final AugmentationTarget schema,
            final Set<QName> qnames) {
        for (final AugmentationSchemaNode augment : schema.getAvailableAugmentations()) {
            if (qnames.equals(augment.getChildNodes().stream()
                .map(DataSchemaNode::getQName)
                .collect(Collectors.toUnmodifiableSet()))) {
                return augment;
            }
        }

        throw new IllegalStateException(
            "Unknown augmentation node detected, identified by: " + qnames + ", in: " + schema);
    }

    // FIXME: 7.0.0: can we get rid of this?
    private static SchemaNode findCaseByChild(final ChoiceSchemaNode parent, final QName qname) {
        for (final CaseSchemaNode caze : parent.getCases()) {
            if (caze.dataChildByName(qname) != null) {
                return caze;
            }
        }
        return null;
    }
}
