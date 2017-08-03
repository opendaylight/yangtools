/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
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
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;
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
        SchemaNode current = SchemaUtils.findParentSchemaOnPath(context, path);
        Preconditions.checkArgument(current instanceof DataNodeContainer,"Schema path must point to container or list or an rpc input/output. Supplied path %s pointed to: %s",path,current);
        root = (DataNodeContainer) current;
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
        if (parent instanceof DataNodeContainer) {
            schema = ((DataNodeContainer)parent).getDataChildByName(qname);
            if (schema == null) {
                if (parent instanceof GroupingDefinition) {
                    schema = (GroupingDefinition) parent;
                } else if (parent instanceof NotificationDefinition) {
                    schema = (NotificationDefinition) parent;
                }
            }
        } else if (parent instanceof ChoiceSchemaNode) {
            schema = findChildInCases((ChoiceSchemaNode) parent, qname);
        } else {
            throw new IllegalStateException("Unsupported schema type "+ parent.getClass() +" on stack.");
        }

        Preconditions.checkArgument(schema != null, "Could not find schema for node %s in %s", qname, parent);
        return schema;
    }

    private static SchemaNode findChildInCases(final ChoiceSchemaNode parent, final QName qname) {
        DataSchemaNode schema = null;
        for (final ChoiceCaseNode caze : parent.getCases()) {
            final DataSchemaNode potential = caze.getDataChildByName(qname);
            if (potential != null) {
                schema = potential;
                break;
            }
        }
        return schema;
    }

    private static SchemaNode findCaseByChild(final ChoiceSchemaNode parent, final QName qname) {
        DataSchemaNode schema = null;
        for (final ChoiceCaseNode caze : parent.getCases()) {
            final DataSchemaNode potential = caze.getDataChildByName(qname);
            if (potential != null) {
                schema = caze;
                break;
            }
        }
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

    @Deprecated
    public LeafListSchemaNode leafSetEntryNode() {
        final Object parent = getParent();

        Preconditions.checkArgument(parent instanceof LeafListSchemaNode, "Not currently in a leaf-list");
        return (LeafListSchemaNode) parent;
    }

    public LeafListSchemaNode leafSetEntryNode(final QName qname) {
        final Object parent = getParent();
        if (parent instanceof LeafListSchemaNode) {
            return (LeafListSchemaNode) parent;
        }

        final SchemaNode child = SchemaUtils.findChildSchemaByQName((SchemaNode) parent, qname);
        Preconditions.checkArgument(child instanceof LeafListSchemaNode,
            "Node %s is neither a leaf-list nor currently in a leaf-list", child.getPath());
        return (LeafListSchemaNode) child;
    }

    public ChoiceSchemaNode startChoiceNode(final NodeIdentifier name) {
        LOG.debug("Enter choice {}", name);
        final SchemaNode schema = getSchema(name);

        Preconditions.checkArgument(schema instanceof ChoiceSchemaNode, "Node %s is not a choice", schema.getPath());
        schemaStack.push(schema);
        return (ChoiceSchemaNode)schema;
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

    public SchemaNode startYangModeledAnyXmlNode(final NodeIdentifier name) {
        LOG.debug("Enter yang modeled anyXml {}", name);
        final SchemaNode schema = getSchema(name);

        Preconditions.checkArgument(schema instanceof YangModeledAnyXmlSchemaNode,
                "Node %s is not an yang modeled anyXml.", schema.getPath());

        schemaStack.push(((YangModeledAnyXmlSchemaNode) schema).getSchemaOfAnyXmlData());

        return schema;
    }

    public AugmentationSchema startAugmentationNode(final AugmentationIdentifier identifier) {
        LOG.debug("Enter augmentation {}", identifier);
        Object parent = getParent();

        Preconditions.checkArgument(parent instanceof AugmentationTarget, "Augmentation not allowed under %s", parent);
        if (parent instanceof ChoiceSchemaNode) {
            final QName name = Iterables.get(identifier.getPossibleChildNames(), 0);
            parent = findCaseByChild((ChoiceSchemaNode) parent, name);
        }
        Preconditions.checkArgument(parent instanceof DataNodeContainer, "Augmentation allowed only in DataNodeContainer",parent);
        final AugmentationSchema schema = SchemaUtils.findSchemaForAugment((AugmentationTarget) parent, identifier.getPossibleChildNames());
        final HashSet<DataSchemaNode> realChildSchemas = new HashSet<>();
        for (final DataSchemaNode child : schema.getChildNodes()) {
            realChildSchemas.add(((DataNodeContainer) parent).getDataChildByName(child.getQName()));
        }
        final AugmentationSchema resolvedSchema = new EffectiveAugmentationSchema(schema, realChildSchemas);
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

}
