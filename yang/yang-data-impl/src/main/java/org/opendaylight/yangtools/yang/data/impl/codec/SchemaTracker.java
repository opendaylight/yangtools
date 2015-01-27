/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.SchemaUtils;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for tracking the underlying state of the underlying
 * schema node.
 */
@Beta
public final class SchemaTracker {
    private static final Logger LOG = LoggerFactory.getLogger(SchemaTracker.class);
    private final Deque<Object> schemaStack = new ArrayDeque<>();
    private final DataNodeContainer root;

    private SchemaTracker(final SchemaContext context, final SchemaPath path) {
        SchemaNode current = Preconditions.checkNotNull(context);
        for (final QName qname : path.getPathFromRoot()) {
            SchemaNode child;
            if(current instanceof DataNodeContainer) {
                child = ((DataNodeContainer) current).getDataChildByName(qname);

                if(child == null && current instanceof SchemaContext) {
                    child = tryFindNotification((SchemaContext) current, qname)
                            .orNull();
                }
            } else if (current instanceof ChoiceNode) {
                child = ((ChoiceNode) current).getCaseNodeByName(qname);
            } else {
                throw new IllegalArgumentException(String.format("Schema node %s does not allow children.", current));
            }
            current = child;
        }
        Preconditions.checkArgument(current instanceof DataNodeContainer,"Schema path must point to container or list. Supplied path %s pointed to: %s",path,current);
        this.root = (DataNodeContainer) current;
    }

    private Optional<SchemaNode> tryFindNotification(final SchemaContext ctx, final QName qname) {
        return Optional.<SchemaNode>fromNullable(Iterables.find(ctx.getNotifications(), new SchemaNodePredicate(qname), null));
    }

    /**
     * Create a new writer with the specified context as its root.
     *
     * @param context Associated {@link SchemaContext}.
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static SchemaTracker create(final SchemaContext context) {
        return create(context, SchemaPath.ROOT);
    }

    /**
     * Create a new writer with the specified context and rooted in the specified schema path
     *
     * @param context Associated {@link SchemaContext}
     * @param path schema path
     *
     * @return A new {@link NormalizedNodeStreamWriter}
     */
    public static SchemaTracker create(final SchemaContext context, final SchemaPath path) {
        return new SchemaTracker(context, path);
    }

    public Object getParent() {
        if (schemaStack.isEmpty()) {
            return root;
        }
        return schemaStack.peek();
    }

    private SchemaNode getSchema(final PathArgument name) {
        final Object parent = getParent();
        SchemaNode schema = null;
        final QName qname = name.getNodeType();
        if(parent instanceof DataNodeContainer) {
            schema = ((DataNodeContainer)parent).getDataChildByName(qname);

            if(schema == null && parent instanceof NotificationDefinition) {
                schema = ((NotificationDefinition) parent);
            }
        } else if(parent instanceof ChoiceNode) {
            for(ChoiceCaseNode caze : ((ChoiceNode) parent).getCases()) {
                DataSchemaNode potential = caze.getDataChildByName(qname);
                if(potential != null) {
                    schema = potential;
                    break;
                }
            }
        } else {
            throw new IllegalStateException("Unsupported schema type "+ parent.getClass() +" on stack.");
        }
        Preconditions.checkArgument(schema != null, "Could not find schema for node %s in %s", qname, parent);
        return schema;
    }

    public void startList(final PathArgument name) {
        final SchemaNode schema = getSchema(name);
        Preconditions.checkArgument(schema instanceof ListSchemaNode, "Node %s is not a list", schema.getPath());
        schemaStack.push(schema);
    }

    public void startListItem(final PathArgument name) throws IOException {
        final Object schema = getParent();
        Preconditions.checkArgument(schema instanceof ListSchemaNode, "List item is not appropriate");
        schemaStack.push(schema);
    }

    public LeafSchemaNode leafNode(final NodeIdentifier name) throws IOException {
        final SchemaNode schema = getSchema(name);

        Preconditions.checkArgument(schema instanceof LeafSchemaNode, "Node %s is not a leaf", schema.getPath());
        return (LeafSchemaNode) schema;
    }

    public LeafListSchemaNode startLeafSet(final NodeIdentifier name) {
        final SchemaNode schema = getSchema(name);

        Preconditions.checkArgument(schema instanceof LeafListSchemaNode, "Node %s is not a leaf-list", schema.getPath());
        schemaStack.push(schema);
        return (LeafListSchemaNode)schema;
    }

    public LeafListSchemaNode leafSetEntryNode() {
        final Object parent = getParent();

        Preconditions.checkArgument(parent instanceof LeafListSchemaNode, "Not currently in a leaf-list");
        return (LeafListSchemaNode) parent;
    }

    public ChoiceNode startChoiceNode(final NodeIdentifier name) {
        LOG.debug("Enter choice {}", name);
        final SchemaNode schema = getSchema(name);

        Preconditions.checkArgument(schema instanceof ChoiceNode, "Node %s is not a choice", schema.getPath());
        schemaStack.push(schema);
        return (ChoiceNode)schema;
    }

    public SchemaNode startContainerNode(final NodeIdentifier name) {
        LOG.debug("Enter container {}", name);
        final SchemaNode schema = getSchema(name);

        boolean isAllowed = schema instanceof ContainerSchemaNode;
        isAllowed |= schema instanceof NotificationDefinition;

        Preconditions.checkArgument(isAllowed, "Node %s is not a container nor a notification", schema.getPath());
        schemaStack.push(schema);
        return schema;
    }

    public AugmentationSchema startAugmentationNode(final AugmentationIdentifier identifier) {
        LOG.debug("Enter augmentation {}", identifier);
        final Object parent = getParent();

        Preconditions.checkArgument(parent instanceof AugmentationTarget, "Augmentation not allowed under %s", parent);
        Preconditions.checkArgument(parent instanceof DataNodeContainer, "Augmentation allowed only in DataNodeContainer",parent);
        final AugmentationSchema schema = SchemaUtils.findSchemaForAugment((AugmentationTarget) parent, identifier.getPossibleChildNames());
        HashSet<DataSchemaNode> realChildSchemas = new HashSet<>();
        for(DataSchemaNode child : schema.getChildNodes()) {
            realChildSchemas.add(((DataNodeContainer) parent).getDataChildByName(child.getQName()));
        }
        AugmentationSchema resolvedSchema = new AugmentationSchemaProxy(schema, realChildSchemas);
        schemaStack.push(resolvedSchema);
        return resolvedSchema;
    }

    public AnyXmlSchemaNode anyxmlNode(final NodeIdentifier name) {
        final SchemaNode schema = getSchema(name);

        Preconditions.checkArgument(schema instanceof AnyXmlSchemaNode, "Node %s is not anyxml", schema.getPath());
        return (AnyXmlSchemaNode)schema;
    }

    public Object endNode() {
        return schemaStack.pop();
    }

    private static final class SchemaNodePredicate implements Predicate<SchemaNode> {
        private final QName qname;

        public SchemaNodePredicate(final QName qname) {
            this.qname = qname;
        }

        @Override
        public boolean apply(final SchemaNode input) {
            return input.getQName().equals(qname);
        }
    }
}
