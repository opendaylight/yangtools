/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

public class DataModificationException extends Exception {
    // TODO replace QName as identifier for node with schema or NodeIdentifier,
    // Augmentation does not have a QName

    private static final long serialVersionUID = 1L;
    private final QName node;

    public DataModificationException(final String message, final QName node) {
        super(message);
        this.node = node;
    }

    public QName getNodeQName() {
        return node;
    }

    public static final class DataMissingException extends DataModificationException {
        private static final long serialVersionUID = 1L;

        public DataMissingException(final QName nodeType) {
            super(String.format("Data missing for node: %s", nodeType), nodeType);
        }

        public DataMissingException(final QName nodeType, final NormalizedNode<?, ?> modificationNode) {
            super(String.format("Data missing for node: %s, %s", nodeType, modificationNode), nodeType);
        }

        static void check(final QName nodeQName, final Optional<? extends NormalizedNode<?, ?>> actualNode) throws DataMissingException {
            if (!actualNode.isPresent()) {
                throw new DataMissingException(nodeQName);
            }
        }

        static void check(final QName nodeQName, final Optional<LeafSetNode<?>> actualNodes, final LeafSetEntryNode<?> modificationNode)
                throws DataMissingException {
            if (!actualNodes.isPresent() || !actualNodes.get().getChild(modificationNode.getIdentifier()).isPresent()) {
                throw new DataMissingException(nodeQName, modificationNode);
            }
        }

        static void check(final QName nodeQName, final Optional<MapNode> actualNodes, final MapEntryNode modificationNode)
                throws DataModificationException {
            if (!actualNodes.isPresent() || !actualNodes.get().getChild(modificationNode.getIdentifier()).isPresent()) {
                throw new DataMissingException(nodeQName, modificationNode);
            }
        }
    }

    public static final class DataExistsException extends DataModificationException {
        private static final long serialVersionUID = 1L;

        public DataExistsException(final QName nodeType, final NormalizedNode<?, ?> actualNode, final NormalizedNode<?, ?> modificationNode) {
            super(String
                    .format("Data already exists for node: %s, current value: %s. modification value: %s", nodeType, actualNode, modificationNode),
                    nodeType);
        }

        static void check(final QName nodeQName, final Optional<? extends NormalizedNode<?, ?>> actualNode, final NormalizedNode<?, ?> modificationNode) throws DataExistsException {
            if (actualNode.isPresent()) {
                throw new DataExistsException(nodeQName, actualNode.get(), modificationNode);
            }
        }

        static void check(final QName nodeQName, final Optional<LeafSetNode<?>> actualNodes, final LeafSetEntryNode<?> modificationNode)
                throws DataExistsException {
            if (actualNodes.isPresent() && actualNodes.get().getChild(modificationNode.getIdentifier()).isPresent()) {
                throw new DataExistsException(nodeQName, actualNodes.get(), modificationNode);
            }
        }

        public static void check(final QName qName, final Optional<MapNode> actualNodes, final MapEntryNode listModification)
                throws DataModificationException {
            if (actualNodes.isPresent() && actualNodes.get().getChild(listModification.getIdentifier()).isPresent()) {
                throw new DataExistsException(qName, actualNodes.get(), listModification);
            }
        }
    }

    public static final class IllegalChoiceValuesException extends DataModificationException {
        private static final long serialVersionUID = 1L;

        public IllegalChoiceValuesException(final String message, final QName node) {
            super(message, node);
        }

        public static void throwMultipleCasesReferenced(final QName choiceQName, final ChoiceNode modification,
                final QName case1QName, final QName case2QName) throws IllegalChoiceValuesException {
            throw new IllegalChoiceValuesException(String.format(
                    "Child nodes from multiple cases present in modification: %s, choice: %s, case1: %s, case2: %s",
                    modification, choiceQName, case1QName, case2QName), choiceQName);
        }

        public static void throwUnknownChild(final QName choiceQName, final QName nodeQName) throws IllegalChoiceValuesException {
            throw new IllegalChoiceValuesException(String.format(
                    "Unknown child node detected, choice: %s, child node: %s",
                    choiceQName, nodeQName), choiceQName);
        }
    }

}
