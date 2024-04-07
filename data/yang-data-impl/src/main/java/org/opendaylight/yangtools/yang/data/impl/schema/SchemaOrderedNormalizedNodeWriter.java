/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaTreeInference;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * This is an iterator over a {@link NormalizedNode}. Unlike {@link NormalizedNodeWriter}, this iterates over elements
 * in the order as they are defined in YANG file.
 */
public class SchemaOrderedNormalizedNodeWriter extends NormalizedNodeWriter {
    private final EffectiveModelContext modelContext;
    private final SchemaNode root;

    private SchemaNode currentSchemaNode;

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}.
     *
     * @param writer Back-end writer
     * @param modelContext Associated {@link EffectiveModelContext}
     */
    public SchemaOrderedNormalizedNodeWriter(final NormalizedNodeStreamWriter writer,
            final EffectiveModelContext modelContext) {
        super(writer);
        root = this.modelContext = requireNonNull(modelContext);
    }

    private SchemaOrderedNormalizedNodeWriter(final NormalizedNodeStreamWriter writer,
            final SchemaInferenceStack stack) {
        super(writer);
        modelContext = stack.modelContext();

        if (!stack.isEmpty()) {
            final var current = stack.currentStatement();
            // FIXME: this should be one of NormalizedNodeContainer/NotificationDefinition/OperationDefinition
            if (current instanceof SchemaNode schemaNode) {
                root = schemaNode;
            } else {
                throw new IllegalArgumentException("Instantiating at " + current + " is not supported");
            }
        } else {
            root = modelContext;
        }
    }

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}.
     *
     * @param writer Back-end writer
     * @param schemaContext Associated {@link EffectiveModelContext}
     * @param path root path
     */
    public SchemaOrderedNormalizedNodeWriter(final NormalizedNodeStreamWriter writer,
            final EffectiveModelContext schemaContext, final Absolute path) {
        this(writer, SchemaInferenceStack.of(schemaContext, path));
    }

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}.
     *
     * @param writer Back-end writer
     * @param rootInference A SchemaTreeInference pointing to the root element
     */
    public SchemaOrderedNormalizedNodeWriter(final NormalizedNodeStreamWriter writer,
            final SchemaTreeInference rootInference) {
        this(writer, SchemaInferenceStack.ofInference(rootInference));
    }

    @Override
    public SchemaOrderedNormalizedNodeWriter write(final NormalizedNode node) throws IOException {
        if (modelContext.equals(root)) {
            currentSchemaNode = modelContext.dataChildByName(node.name().getNodeType());
        } else {
            currentSchemaNode = root;
        }
        return write(node, currentSchemaNode);
    }

    /**
     * Iterate over the provided collection and emit write
     * events to the encapsulated {@link NormalizedNodeStreamWriter}.
     *
     * @param nodes nodes
     * @return NormalizedNodeWriter this
     * @throws IOException when thrown from the backing writer.
     */
    public SchemaOrderedNormalizedNodeWriter write(final Collection<DataContainerChild> nodes) throws IOException {
        currentSchemaNode = root;
        if (writeChildren(nodes, currentSchemaNode, false)) {
            return this;
        }

        throw new IllegalStateException("It wasn't possible to serialize nodes " + nodes);
    }

    private SchemaOrderedNormalizedNodeWriter write(final NormalizedNode node, final SchemaNode dataSchemaNode)
            throws IOException {

        //Set current schemaNode
        try (var sns = new SchemaNodeSetter(dataSchemaNode)) {
            if (node != null) {
                return write(node);
            }
        }

        throw new IllegalStateException("It wasn't possible to serialize node " + node);
    }

    private void write(final Collection<NormalizedNode> nodes, final SchemaNode dataSchemaNode) throws IOException {
        for (var node : nodes) {
            write(node, dataSchemaNode);
        }
    }

    @Override
    protected void writeChildren(final Iterable<? extends NormalizedNode> children) throws IOException {
        writeChildren(children, currentSchemaNode, true);
    }

    private boolean writeChildren(final Iterable<? extends NormalizedNode> children, final SchemaNode parentSchemaNode,
            final boolean endParent) throws IOException {
        // Augmentations cannot be gotten with node.getChild so create our own structure with augmentations resolved
        final var qnameToNodes = ArrayListMultimap.<QName, NormalizedNode>create();
        for (var child : children) {
            putChild(qnameToNodes, child);
        }

        if (parentSchemaNode instanceof DataNodeContainer parentContainer) {
            if (parentContainer instanceof ListSchemaNode && qnameToNodes.containsKey(parentSchemaNode.getQName())) {
                write(qnameToNodes.get(parentSchemaNode.getQName()), parentSchemaNode);
            } else {
                for (var schemaNode : parentContainer.getChildNodes()) {
                    write(qnameToNodes.get(schemaNode.getQName()), schemaNode);
                }
            }
        } else if (parentSchemaNode instanceof ChoiceSchemaNode parentChoice) {
            for (var childCase : parentChoice.getCases()) {
                for (var childCaseChild : childCase.getChildNodes()) {
                    final var node = qnameToNodes.asMap().get(childCaseChild.getQName());
                    if (node != null) {
                        write(node, childCaseChild);
                    }
                }
            }
        } else {
            for (var child : children) {
                writeLeaf(child);
            }
        }
        if (endParent) {
            writer.endNode();
        }
        return true;
    }

    private SchemaOrderedNormalizedNodeWriter writeLeaf(final NormalizedNode node) throws IOException {
        if (wasProcessAsSimpleNode(node)) {
            return this;
        }

        throw new IllegalStateException("It wasn't possible to serialize node " + node);
    }

    private static void putChild(final Multimap<QName, NormalizedNode> qnameToNodes, final NormalizedNode child) {
        qnameToNodes.put(child.name().getNodeType(), child);
    }

    private final class SchemaNodeSetter implements AutoCloseable {
        private final SchemaNode previousSchemaNode;

        /**
         * Sets current schema node new value and store old value for later restore.
         */
        SchemaNodeSetter(final SchemaNode schemaNode) {
            previousSchemaNode = currentSchemaNode;
            currentSchemaNode = schemaNode;
        }

        /**
         * Restore previous schema node.
         */
        @Override
        public void close() {
            currentSchemaNode = previousSchemaNode;
        }
    }
}
