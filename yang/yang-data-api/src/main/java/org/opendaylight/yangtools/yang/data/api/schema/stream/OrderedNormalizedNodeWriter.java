/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import java.io.IOException;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

public class OrderedNormalizedNodeWriter extends NormalizedNodeWriter{

    private final SchemaContext schemaContext;


    public OrderedNormalizedNodeWriter(NormalizedNodeStreamWriter writer, SchemaContext schemaContext) {
        super(writer);
        this.schemaContext = schemaContext;
    }

    @Override
    public NormalizedNodeWriter write(NormalizedNode<?, ?> node) throws IOException {
        SchemaNode dataSchemaNode = schemaContext.getDataChildByName(node.getNodeType());
        Preconditions.checkNotNull(dataSchemaNode, "Node type not found in schemaContext");
        return doWrite(node, dataSchemaNode);
    }

    /**
     * Should be used instead of write(NormalizedNode node) for rpc's or schemaNodes not directly found in schemaContext
     * @param node
     * @param dataSchemaNode
     * @return
     * @throws IOException
     */
    public NormalizedNodeWriter write(NormalizedNode<?, ?> node, SchemaNode dataSchemaNode) throws IOException {
        Preconditions.checkNotNull(dataSchemaNode, "Node type not found in schemaContext");
        return doWrite(node, dataSchemaNode);
    }

    private NormalizedNodeWriter doWrite(NormalizedNode<?, ?> node, SchemaNode dataSchemaNode) throws IOException {
        if (node == null) {
            return this;
        }

        if (wasProcessedAsCompositeNode(node, dataSchemaNode)) {
            return this;
        }

        if (wasProcessAsSimpleNode(node)) {
            return this;
        }

        throw new IllegalStateException("It wasn't possible to serialize node " + node);
    }

    private void write(List<NormalizedNode<?, ?>> nodes, SchemaNode dataSchemaNode) throws IOException {
        for (NormalizedNode<?, ?> node : nodes) {
            doWrite(node, dataSchemaNode);
        }
    }

    private NormalizedNodeWriter writeLeaf(final NormalizedNode<?, ?> node) throws IOException {
        if (wasProcessAsSimpleNode(node)) {
            return this;
        }

        throw new IllegalStateException("It wasn't possible to serialize node " + node);
    }

    private boolean writeChildren(final Iterable<? extends NormalizedNode<?, ?>> children, SchemaNode parentSchemaNode) throws IOException {
        //Augmentations cannot be gotten with node.getChild so create our own structure with augmentations resolved
        ArrayListMultimap<QName, NormalizedNode<?, ?>> qNameToNodes = ArrayListMultimap.create();
        for (NormalizedNode<?, ?> child : children) {
            if (child instanceof AugmentationNode) {
                qNameToNodes.putAll(resolveAugmentations(child));
            } else {
                qNameToNodes.put(child.getNodeType(), child);
            }
        }

        if (parentSchemaNode instanceof DataNodeContainer) {
            if (parentSchemaNode instanceof ListSchemaNode && qNameToNodes.containsKey(parentSchemaNode.getQName())) {
                write(qNameToNodes.get(parentSchemaNode.getQName()), parentSchemaNode);
            } else {
                for (DataSchemaNode schemaNode : ((DataNodeContainer) parentSchemaNode).getChildNodes()) {
                    write(qNameToNodes.get(schemaNode.getQName()), schemaNode);
                }
            }
        } else if(parentSchemaNode instanceof ChoiceSchemaNode) {
            for (ChoiceCaseNode ccNode : ((ChoiceSchemaNode) parentSchemaNode).getCases()) {
                for (DataSchemaNode dsn : ccNode.getChildNodes()) {
                    if (qNameToNodes.containsKey(dsn.getQName())) {
                        write(qNameToNodes.get(dsn.getQName()), dsn);
                    }
                }
            }
        } else {
            for (NormalizedNode<?, ?> child : children) {
                writeLeaf(child);
            }
        }

        writer.endNode();
        return true;
    }

    private ArrayListMultimap<QName, NormalizedNode<?, ?>> resolveAugmentations(NormalizedNode<?, ?> child) {
        final ArrayListMultimap<QName, NormalizedNode<?, ?>> resolvedAugs = ArrayListMultimap.create();
        for (NormalizedNode<?, ?> node : ((AugmentationNode) child).getValue()) {
            if (node instanceof AugmentationNode) {
                resolvedAugs.putAll(resolveAugmentations(node));
            } else {
                resolvedAugs.put(node.getNodeType(), node);
            }
        }
        return resolvedAugs;
    }

    protected boolean writeMapEntryNode(final MapEntryNode node, final SchemaNode dataSchemaNode) throws IOException {
        if(writer instanceof NormalizedNodeStreamAttributeWriter) {
            ((NormalizedNodeStreamAttributeWriter) writer)
                    .startMapEntryNode(node.getIdentifier(), childSizeHint(node.getValue()), node.getAttributes());
        } else {
            writer.startMapEntryNode(node.getIdentifier(), childSizeHint(node.getValue()));
        }
        return writeChildren(node.getValue(), dataSchemaNode);
    }

    private boolean wasProcessAsSimpleNode(final NormalizedNode<?, ?> node) throws IOException {
        if (node instanceof LeafSetEntryNode) {
            final LeafSetEntryNode<?> nodeAsLeafList = (LeafSetEntryNode<?>)node;
            if(writer instanceof NormalizedNodeStreamAttributeWriter) {
                ((NormalizedNodeStreamAttributeWriter) writer).leafSetEntryNode(nodeAsLeafList.getValue(), nodeAsLeafList.getAttributes());
            } else {
                writer.leafSetEntryNode(nodeAsLeafList.getValue());
            }
            return true;
        } else if (node instanceof LeafNode) {
            final LeafNode<?> nodeAsLeaf = (LeafNode<?>)node;
            if(writer instanceof NormalizedNodeStreamAttributeWriter) {
                ((NormalizedNodeStreamAttributeWriter) writer).leafNode(nodeAsLeaf.getIdentifier(), nodeAsLeaf.getValue(), nodeAsLeaf.getAttributes());
            } else {
                writer.leafNode(nodeAsLeaf.getIdentifier(), nodeAsLeaf.getValue());
            }
            return true;
        } else if (node instanceof AnyXmlNode) {
            final AnyXmlNode anyXmlNode = (AnyXmlNode)node;
            writer.anyxmlNode(anyXmlNode.getIdentifier(), anyXmlNode.getValue());
            return true;
        }

        return false;
    }

    private boolean wasProcessedAsCompositeNode(final NormalizedNode<?, ?> node, SchemaNode dataSchemaNode) throws IOException {
        if (node instanceof ContainerNode) {
            final ContainerNode n = (ContainerNode) node;
            if(writer instanceof NormalizedNodeStreamAttributeWriter) {
                ((NormalizedNodeStreamAttributeWriter) writer).startContainerNode(n.getIdentifier(), childSizeHint(n.getValue()), n.getAttributes());
            } else {
                writer.startContainerNode(n.getIdentifier(), childSizeHint(n.getValue()));
            }
            return writeChildren(n.getValue(), dataSchemaNode);
        }
        if (node instanceof MapEntryNode) {
            return writeMapEntryNode((MapEntryNode) node, dataSchemaNode);
        }
        if (node instanceof UnkeyedListEntryNode) {
            final UnkeyedListEntryNode n = (UnkeyedListEntryNode) node;
            writer.startUnkeyedListItem(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue(), dataSchemaNode);
        }
        if (node instanceof ChoiceNode) {
            final ChoiceNode n = (ChoiceNode) node;
            writer.startChoiceNode(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue(), dataSchemaNode);
        }
        if (node instanceof AugmentationNode) {
            final AugmentationNode n = (AugmentationNode) node;
            writer.startAugmentationNode(n.getIdentifier());
            return writeChildren(n.getValue(), dataSchemaNode);
        }
        if (node instanceof UnkeyedListNode) {
            final UnkeyedListNode n = (UnkeyedListNode) node;
            writer.startUnkeyedList(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue(), dataSchemaNode);
        }
        if (node instanceof OrderedMapNode) {
            final OrderedMapNode n = (OrderedMapNode) node;
            writer.startOrderedMapNode(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue(), dataSchemaNode);
        }
        if (node instanceof MapNode) {
            final MapNode n = (MapNode) node;
            writer.startMapNode(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue(), dataSchemaNode);
        }
        if (node instanceof LeafSetNode) {
            //covers also OrderedLeafSetNode for which doesn't exist start* method
            final LeafSetNode<?> n = (LeafSetNode<?>) node;
            writer.startLeafSet(n.getIdentifier(), childSizeHint(n.getValue()));
            return writeChildren(n.getValue(), dataSchemaNode);
        }

        return false;
    }
}