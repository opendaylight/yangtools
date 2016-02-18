/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import com.google.common.collect.ArrayListMultimap;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class YangOrderedNormalizedNodeWriter extends NormalizedNodeWriter {

    private final SchemaContext schemaContext;
    private final SchemaNode root;
    private final NormalizedNodeStreamWriter writer;

    private SchemaNode currentSchemaNode;

    public YangOrderedNormalizedNodeWriter(final NormalizedNodeStreamWriter writer, final SchemaContext schemaContext, final SchemaPath path) {
        super(writer);
        this.writer = writer;
        this.schemaContext = schemaContext;
        this.root = SchemaUtils.findParentSchemaOnPath(schemaContext, path);
    }

    public YangOrderedNormalizedNodeWriter write(final NormalizedNode<?, ?> node) throws IOException {
        if (Objects.equals(root, schemaContext)) {
            currentSchemaNode = schemaContext.getDataChildByName(node.getNodeType());
        } else {
            currentSchemaNode = root;
        }
        return write(node, currentSchemaNode);

    }

    public YangOrderedNormalizedNodeWriter write(final Collection<DataContainerChild<?,?>> nodes) throws IOException {
        currentSchemaNode = root;
        if (writeChildren(nodes, currentSchemaNode, false)) {
            return this;
        }

        throw new IllegalStateException("It wasn't possible to serialize nodes " + nodes);

    }

    private YangOrderedNormalizedNodeWriter write(final NormalizedNode<?, ?> node, final SchemaNode dataSchemaNode) throws IOException {

        //Set current schemaNode
        try (SchemaNodeSetter sns = new SchemaNodeSetter(dataSchemaNode)) {
            if (node == null) {
                return this;
            }

            if (wasProcessedAsCompositeNode(node)) {
                return this;
            }

            if (wasProcessAsSimpleNode(node)) {
                return this;
            }
        }

        throw new IllegalStateException("It wasn't possible to serialize node " + node);
    }

    private void write(final List<NormalizedNode<?, ?>> nodes, final SchemaNode dataSchemaNode) throws IOException {
        for (NormalizedNode<?, ?> node : nodes) {
            write(node, dataSchemaNode);
        }
    }

    protected boolean writeChildren(final Iterable<? extends NormalizedNode<?, ?>> children) throws IOException {
        return writeChildren(children, currentSchemaNode, true);
    }

    private boolean writeChildren(final Iterable<? extends NormalizedNode<?, ?>> children, final SchemaNode parentSchemaNode, boolean endParent) throws IOException {
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
        if (endParent) {
            writer.endNode();
        }
        return true;
    }

    private YangOrderedNormalizedNodeWriter writeLeaf(final NormalizedNode<?, ?> node) throws IOException {
        if (wasProcessAsSimpleNode(node)) {
            return this;
        }

        throw new IllegalStateException("It wasn't possible to serialize node " + node);
    }

    private ArrayListMultimap<QName, NormalizedNode<?, ?>> resolveAugmentations(final NormalizedNode<?, ?> child) {
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


    private class SchemaNodeSetter implements AutoCloseable {

        private final SchemaNode previousSchemaNode;

        /**
         * Sets current schema node new value and store old value for later restore
         */
        public SchemaNodeSetter(final SchemaNode schemaNode) {
            previousSchemaNode = YangOrderedNormalizedNodeWriter.this.currentSchemaNode;
            YangOrderedNormalizedNodeWriter.this.currentSchemaNode = schemaNode;
        }

        /**
         * Restore previous schema node
         */
        @Override
        public void close() {
            YangOrderedNormalizedNodeWriter.this.currentSchemaNode = previousSchemaNode;
        }
    }

}