/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkArgument;
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
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaTreeInference;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * This is an iterator over a {@link NormalizedNode}. Unlike {@link NormalizedNodeWriter}, this iterates over elements
 * in the order as they are defined in YANG file.
 */
public class SchemaOrderedNormalizedNodeWriter extends NormalizedNodeWriter {
    private final EffectiveModelContext schemaContext;
    private final SchemaNode root;

    private SchemaNode currentSchemaNode;

    /**
     * Create a new writer backed by a {@link NormalizedNodeStreamWriter}.
     *
     * @param writer Back-end writer
     * @param schemaContext Associated {@link EffectiveModelContext}
     */
    public SchemaOrderedNormalizedNodeWriter(final NormalizedNodeStreamWriter writer,
            final EffectiveModelContext schemaContext) {
        super(writer);
        root = this.schemaContext = requireNonNull(schemaContext);
    }

    private SchemaOrderedNormalizedNodeWriter(final NormalizedNodeStreamWriter writer,
            final SchemaInferenceStack stack) {
        super(writer);
        schemaContext = stack.getEffectiveModelContext();

        if (!stack.isEmpty()) {
            final EffectiveStatement<?, ?> current = stack.currentStatement();
            // FIXME: this should be one of NormalizedNodeContainer/NotificationDefinition/OperationDefinition
            checkArgument(current instanceof SchemaNode, "Instantiating at %s is not supported", current);
            root = (SchemaNode) current;
        } else {
            root = schemaContext;
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
        if (schemaContext.equals(root)) {
            currentSchemaNode = schemaContext.dataChildByName(node.getIdentifier().getNodeType());
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

    private void write(final Collection<NormalizedNode> nodes, final SchemaNode dataSchemaNode) throws IOException {
        for (final NormalizedNode node : nodes) {
            write(node, dataSchemaNode);
        }
    }

    @Override
    protected boolean writeChildren(final Iterable<? extends NormalizedNode> children) throws IOException {
        return writeChildren(children, currentSchemaNode, true);
    }

    private boolean writeChildren(final Iterable<? extends NormalizedNode> children, final SchemaNode parentSchemaNode,
            final boolean endParent) throws IOException {
        // Augmentations cannot be gotten with node.getChild so create our own structure with augmentations resolved
        final Multimap<QName, NormalizedNode> qnameToNodes = ArrayListMultimap.create();
        for (final NormalizedNode child : children) {
            putChild(qnameToNodes, child);
        }

        if (parentSchemaNode instanceof DataNodeContainer) {
            if (parentSchemaNode instanceof ListSchemaNode && qnameToNodes.containsKey(parentSchemaNode.getQName())) {
                write(qnameToNodes.get(parentSchemaNode.getQName()), parentSchemaNode);
            } else {
                for (final DataSchemaNode schemaNode : ((DataNodeContainer) parentSchemaNode).getChildNodes()) {
                    write(qnameToNodes.get(schemaNode.getQName()), schemaNode);
                }
            }
        } else if (parentSchemaNode instanceof ChoiceSchemaNode) {
            for (final CaseSchemaNode ccNode : ((ChoiceSchemaNode) parentSchemaNode).getCases()) {
                for (final DataSchemaNode dsn : ccNode.getChildNodes()) {
                    if (qnameToNodes.containsKey(dsn.getQName())) {
                        write(qnameToNodes.get(dsn.getQName()), dsn);
                    }
                }
            }
        } else {
            for (final NormalizedNode child : children) {
                writeLeaf(child);
            }
        }
        if (endParent) {
            getWriter().endNode();
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
        qnameToNodes.put(child.getIdentifier().getNodeType(), child);
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
