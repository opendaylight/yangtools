/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import com.google.common.base.Optional;

public class DataModificationException extends Exception {

    private final QName node;

    public DataModificationException(String message, QName node) {
        super(message);
        this.node = node;
    }

    public QName getNodeQName() {
        return node;
    }

    public static final class DataMissingException extends DataModificationException {

        public DataMissingException(QName nodeType) {
            super(String.format("Data missing for node: %s", nodeType), nodeType);
        }

        public DataMissingException(QName nodeType, Node<?> modificationNode) {
            super(String.format("Data missing for node: %s, %s", nodeType, modificationNode), nodeType);
        }

        static void check(QName nodeQName, Optional<? extends NormalizedNode<?, ?>> actualNode) throws DataMissingException {
            if (actualNode.isPresent() == false) {
                throw new DataMissingException(nodeQName);
            }
        }

        static void check(QName nodeQName, Optional<LeafSetNode<?>> actualNodes, LeafSetEntryNode<?> modificationNode)
                throws DataMissingException {
            if (actualNodes.isPresent()==false || actualNodes.get().getChild(modificationNode.getIdentifier()).isPresent() == false) {
                throw new DataMissingException(nodeQName, modificationNode);
            }
        }

        static void check(QName nodeQName, Optional<MapNode> actualNodes, MapEntryNode modificationNode)
                throws DataModificationException {
            if (actualNodes.isPresent()==false || actualNodes.get().getChild(modificationNode.getIdentifier()).isPresent() == false) {
                throw new DataMissingException(nodeQName, modificationNode);
            }
        }
    }

    public static final class DataExistsException extends DataModificationException {

        public DataExistsException(QName nodeType, NormalizedNode<?, ?> actualNode, NormalizedNode<?, ?> modificationNode) {
            super(String
                    .format("Data already exists for node: %s, current value: %s. modification value: %s", nodeType, actualNode, modificationNode),
                    nodeType);
        }

        static void check(QName nodeQName, Optional<? extends NormalizedNode<?, ?>> actualNode, NormalizedNode<?, ?> modificationNode) throws DataExistsException {
            if (actualNode.isPresent()) {
                throw new DataExistsException(nodeQName, actualNode.get(), modificationNode);
            }
        }

        static void check(QName nodeQName, Optional<LeafSetNode<?>> actualNodes, LeafSetEntryNode<?> modificationNode)
                throws DataExistsException {
            if (actualNodes.isPresent() && actualNodes.get().getChild(modificationNode.getIdentifier()).isPresent()) {
                throw new DataExistsException(nodeQName, actualNodes.get(), modificationNode);
            }
        }

        public static void check(QName qName, Optional<MapNode> actualNodes, MapEntryNode listModification)
                throws DataModificationException {
            if (actualNodes.isPresent() && actualNodes.get().getChild(listModification.getIdentifier()).isPresent()) {
                throw new DataExistsException(qName, actualNodes.get(), listModification);
            }
        }
    }

    public static final class IllegalChoiceValuesException extends DataModificationException {

        public IllegalChoiceValuesException(String message, QName node) {
            super(message, node);
        }

        public static void throwMultipleCasesReferenced(QName choiceQName, ChoiceNode modification,
                QName case1QName, QName case2QName) throws IllegalChoiceValuesException {
            throw new IllegalChoiceValuesException(String.format(
                    "Child nodes from multiple cases present in modification: %s, choice: %s, case1: %s, case2: %s",
                    modification, choiceQName, case1QName, case2QName), choiceQName);
        }

        public static void throwUnknownChild(QName choiceQName, QName nodeQName) throws IllegalChoiceValuesException {
            throw new IllegalChoiceValuesException(String.format(
                    "Unknown child node detected, choice: %s, child node: %s",
                    choiceQName, nodeQName), choiceQName);
        }
    }

}
