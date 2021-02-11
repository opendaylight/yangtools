/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
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
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for tracking the underlying state of the underlying schema node.
 */
@Beta
public final class SchemaTracker {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaTracker.class);

    private final Deque<WithStatus> schemaStack = new ArrayDeque<>();
    private final SchemaInferenceStack dataTree;
    private final DataNodeContainer root;

    private SchemaTracker(final SchemaInferenceStack dataTree) {
        this.dataTree = requireNonNull(dataTree);
        if (!dataTree.isEmpty()) {
            final EffectiveStatement<QName, ?> current = dataTree.currentStatement();
            checkArgument(current instanceof DataNodeContainer, "Cannot instantiate on %s", current);

            root = (DataNodeContainer) current;
        } else {
            root = dataTree.getEffectiveModelContext();
        }
    }

    // FIXME: this is quite broken!
    @Deprecated
    private SchemaTracker(final EffectiveModelContext context, final DataNodeContainer root) {
        this.dataTree = new SchemaInferenceStack(context);
        this.root = requireNonNull(root);
    }

    /**
     * Create a new writer with the specified inference state as its root.
     *
     * @param root Root inference state
     * @return A new {@link NormalizedNodeStreamWriter}
     * @throws NullPointerException if {@code root} is null
     */
    public static @NonNull SchemaTracker create(final SchemaInferenceStack root) {
        return new SchemaTracker(root.copy());
    }

    /**
     * Create a new writer with the specified node as its root.
     *
     * @param root Root node
     * @return A new {@link NormalizedNodeStreamWriter}
     * @throws NullPointerException if {@code root} is null
     */
    @Deprecated
    // FIXME: this is quite broken!
    public static @NonNull SchemaTracker create(final DataNodeContainer root) {
        return new SchemaTracker(null, root);
    }

    /**
     * Create a new writer at the root of specified {@link EffectiveModelContext}.
     *
     * @param context effective model context
     * @return A new {@link NormalizedNodeStreamWriter}
     * @throws NullPointerException if {@code context} is null
     */
    public static @NonNull SchemaTracker create(final EffectiveModelContext context) {
        return new SchemaTracker(new SchemaInferenceStack(context));
    }

    /**
     * Create a new writer with the specified context and rooted in the specified schema path.
     *
     * @param context Associated {@link EffectiveModelContext}
     * @param path schema path
     * @return A new {@link SchemaTracker}
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if {@code path} does not point to a valid root
     */
    public static @NonNull SchemaTracker create(final EffectiveModelContext context, final Absolute path) {
        return new SchemaTracker(SchemaInferenceStack.of(context, path));
    }

    /**
     * Create a new writer with the specified context and rooted in the specified schema path.
     *
     * @param context Associated {@link EffectiveModelContext}
     * @param path schema path
     * @return A new {@link SchemaTracker}
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if {@code path} does not point to a valid root
     */
    public static @NonNull SchemaTracker create(final EffectiveModelContext context, final SchemaPath path) {
        return new SchemaTracker(SchemaInferenceStack.ofInstantiatedPath(context, path));
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
    public static @NonNull SchemaTracker forOperation(final EffectiveModelContext context, final Absolute operation,
            final QName qname) {
        final SchemaInferenceStack stack = SchemaInferenceStack.of(context, operation);
        final EffectiveStatement<QName, ?> current = stack.currentStatement();
        checkArgument(current instanceof RpcEffectiveStatement || current instanceof ActionEffectiveStatement,
            "Path %s resolved into non-operation %s", operation, current);
        stack.enterSchemaTree(qname);
        return new SchemaTracker(stack);
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
        if (parent instanceof ChoiceSchemaNode) {
            final DataSchemaNode check = ((ChoiceSchemaNode) parent).findDataSchemaChild(qname).orElse(null);
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
        if (parent instanceof LeafListSchemaNode) {
            return (LeafListSchemaNode) parent;
        }

        // FIXME: when would this trigger?
        final SchemaNode child = SchemaUtils.findDataChildSchemaByQName((SchemaNode) parent, qname);
        checkArgument(child instanceof LeafListSchemaNode,
            "Node %s is neither a leaf-list nor currently in a leaf-list", child);
        return (LeafListSchemaNode) child;
    }

    public void startLeafSetEntryNode(final NodeWithValue<?> name) {
        schemaStack.push(leafSetEntryNode(name.getNodeType()));
    }

    public ChoiceSchemaNode startChoiceNode(final NodeIdentifier name) {
        LOG.debug("Enter choice {}", name);

        final QName qname = name.getNodeType();
        final Object parent = getParent();
        final Object result = parent instanceof ChoiceSchemaNode
            ? startChoiceNode((ChoiceSchemaNode) parent, qname) : startChoiceNode(qname);
        checkArgument(result instanceof ChoiceSchemaNode, "Node %s is not a choice", result);
        final ChoiceSchemaNode ret = (ChoiceSchemaNode)result;
        schemaStack.push(ret);
        return ret;
    }

    private SchemaTreeEffectiveStatement<?> startChoiceNode(final QName qname) {
        final SchemaTreeEffectiveStatement<?> stmt = dataTree.enterSchemaTree(qname);
        dataTree.exit();
        return stmt;
    }

    private static DataSchemaNode startChoiceNode(final ChoiceSchemaNode parent, final QName qname) {
        final Optional<DataSchemaNode> schema = parent.findDataSchemaChild(qname);
        checkArgument(schema.isPresent(), "Could not find schema for node %s in %s", qname, parent);
        return schema.orElseThrow();
    }

    public SchemaNode startContainerNode(final NodeIdentifier name) {
        LOG.debug("Enter container {}", name);
        final SchemaNode schema = enterDataTree(name);
        checkArgument(schema instanceof ContainerLike || schema instanceof NotificationDefinition,
            "Node %s is not a container nor a notification", schema);
        schemaStack.push(schema);
        return schema;
    }

    public AugmentationSchemaNode startAugmentationNode(final AugmentationIdentifier identifier) {
        LOG.debug("Enter augmentation {}", identifier);
        Object parent = getParent();

        checkArgument(parent instanceof AugmentationTarget, "Augmentation not allowed under %s", parent);
        if (parent instanceof ChoiceSchemaNode) {
            final QName name = Iterables.get(identifier.getPossibleChildNames(), 0);
            parent = findCaseByChild((ChoiceSchemaNode) parent, name);
        }
        checkArgument(parent instanceof DataNodeContainer, "Augmentation allowed only in DataNodeContainer", parent);
        final AugmentationSchemaNode schema = SchemaUtils.findSchemaForAugment((AugmentationTarget) parent,
            identifier.getPossibleChildNames());
        final AugmentationSchemaNode resolvedSchema = EffectiveAugmentationSchema.create(schema,
            (DataNodeContainer) parent);
        schemaStack.push(resolvedSchema);
        return resolvedSchema;
    }

    private static SchemaNode findCaseByChild(final ChoiceSchemaNode parent, final QName qname) {
        for (final CaseSchemaNode caze : parent.getCases()) {
            final Optional<DataSchemaNode> potential = caze.findDataChildByName(qname);
            if (potential.isPresent()) {
                return caze;
            }
        }
        return null;
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
        if (ret instanceof DataTreeEffectiveStatement) {
            dataTree.exit();
        }
        return ret;
    }
}
