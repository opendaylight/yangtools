/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.MutableSimpleNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.NodeModification;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;

/**
 * @author michal.rehak
 *
 * @deprecated Use {@link Builders} instead.
 */
@Deprecated
public abstract class NodeFactory {

    /**
     * @param qName
     * @param parent
     * @param value
     * @return simple node modification, based on given qname, value and parent
     */
    public static <T> SimpleNode<T> createImmutableSimpleNode(final QName qName,
            final CompositeNode parent, final T value) {
        return createImmutableSimpleNode(qName, parent, value, null);
    }

    /**
     * @param qName
     * @param parent
     * @param value
     * @param modifyAction
     * @param original originating node, if available
     * @return simple node modification, based on given qname, value and parent
     */
    public static <T> MutableSimpleNode<T> createMutableSimpleNode(final QName qName,
            final CompositeNode parent, final Object value, final ModifyAction modifyAction, final SimpleNode<T> original) {
        @SuppressWarnings("unchecked")
        MutableSimpleNodeTOImpl<T> simpleNodeTOImpl = new MutableSimpleNodeTOImpl<>(qName, parent, (T) value, modifyAction);
        simpleNodeTOImpl.setOriginal(original);
        return simpleNodeTOImpl;
    }

    /**
     * @param qName
     * @param parent
     * @param value
     * @return composite node modification, based on given qname, value (children), parent and modifyAction
     */
    public static CompositeNode createImmutableCompositeNode(final QName qName,
            final CompositeNode parent, final List<Node<?>> value) {
        return createImmutableCompositeNode(qName, parent, value, null);
    }

    /**
     * @param qName
     * @param parent
     * @param valueArg
     * @param modifyAction
     * @param original originating node, if available
     * @return composite node modification, based on given qName, value (children), parent and modifyAction
     */
    public static MutableCompositeNode createMutableCompositeNode(final QName qName,
            final CompositeNode parent, final List<Node<?>> valueArg, final ModifyAction modifyAction, final CompositeNode original) {
        List<Node<?>> value = valueArg;
        if (value == null) {
            value = new ArrayList<>();
        }
        MutableCompositeNodeTOImpl compositeNodeTOImpl =
                new MutableCompositeNodeTOImpl(qName, parent, value, modifyAction);
        compositeNodeTOImpl.setOriginal(original);
        return compositeNodeTOImpl;
    }


    /**
     * @param qName
     * @param parent
     * @param value
     * @param modifyAction
     * @return simple node modification, based on given qname, value, parent and modifyAction
     */
    public static <T> SimpleNode<T> createImmutableSimpleNode(final QName qName,
            final CompositeNode parent, final T value, final ModifyAction modifyAction) {
        SimpleNodeTOImpl<T> simpleNodeModTOImpl =
                new SimpleNodeTOImpl<T>(qName, parent, value, modifyAction);
        return simpleNodeModTOImpl;
    }

    /**
     * @param qName
     * @param parent
     * @param value
     * @param modifyAction
     * @return composite node modification, based on given qname, value (children), parent and modifyAction
     */
    public static CompositeNode createImmutableCompositeNode(final QName qName,
            final CompositeNode parent, final List<Node<?>> value, final ModifyAction modifyAction) {
        return new CompositeNodeTOImpl(qName, parent, value, modifyAction);
    }

    /**
     * @param node
     * @return copy of given node, parent and value are the same, but parent
     * has no reference to this copy
     */
    public static <T> SimpleNode<T> copyNode(final SimpleNode<T> node) {
        return createImmutableSimpleNode(node.getNodeType(), node.getParent(), node.getValue());
    }

    /**
     * @param node
     * @return copy of given node, parent and value are the same, but parent
     * has no reference to this copy
     */
    public static <T> MutableSimpleNode<T> copyNodeAsMutable(final SimpleNode<T> node) {
        return createMutableSimpleNode(
                node.getNodeType(), node.getParent(), node.getValue(),
                node.getModificationAction(), null);
    }

    /**
     * @param node
     * @param children
     * @return copy of given node, parent and children are the same, but parent and children
     * have no reference to this copy
     */
    public static CompositeNode copyNode(final CompositeNode node, final Node<?>... children) {
        CompositeNode twinNode = createImmutableCompositeNode(
                node.getNodeType(), node.getParent(), Arrays.asList(children), node.getModificationAction());
        return twinNode;
    }

    /**
     * @param node
     * @return copy of given node, parent and children are the same, but parent and children
     * have no reference to this copy
     */
    public static CompositeNode copyNode(final CompositeNode node) {
        return copyNode(node, node.getValue().toArray(new Node<?>[0]));
    }

    /**
     * @param node root of original tree
     * @param originalToCopyArg (optional) empty map, where binding between original and copy
     * will be stored
     * @return copy of given node and all subnodes recursively
     */
    public static MutableCompositeNode copyDeepAsMutable(final CompositeNode node,
            final Map<Node<?>, Node<?>> originalToCopyArg) {

        Map<Node<?>, Node<?>> originalToCopy = originalToCopyArg;
        if (originalToCopy == null) {
            originalToCopy = new HashMap<>();
        }

        MutableCompositeNode mutableRoot = createMutableCompositeNode(node.getNodeType(), null, null,
                node.getModificationAction(), null);
        final Deque<SimpleEntry<CompositeNode, MutableCompositeNode>> jobQueue = new ArrayDeque<>();
        jobQueue.push(new SimpleEntry<CompositeNode, MutableCompositeNode>(node, mutableRoot));
        originalToCopy.put(node, mutableRoot);

        while (!jobQueue.isEmpty()) {
            SimpleEntry<CompositeNode, MutableCompositeNode> job = jobQueue.pop();
            CompositeNode originalNode = job.getKey();
            MutableCompositeNode mutableNode = job.getValue();
            mutableNode.setValue(new ArrayList<Node<?>>());

            for (Node<?> child : originalNode.getValue()) {
                Node<?> mutableAscendant = null;
                if (child instanceof CompositeNode) {
                    MutableCompositeNode newMutable =
                            createMutableCompositeNode(child.getNodeType(), mutableNode, null,
                                    ((NodeModification) child).getModificationAction(), null);
                    jobQueue.push(new SimpleEntry<CompositeNode, MutableCompositeNode>(
                            (CompositeNode) child, newMutable));
                    mutableAscendant = newMutable;
                } else if (child instanceof SimpleNode<?>) {
                    mutableAscendant =
                            createMutableSimpleNode(child.getNodeType(), mutableNode,
                                    child.getValue(),
                                    ((NodeModification) child).getModificationAction(), null);
                } else {
                    throw new IllegalStateException("Node class deep copy not supported: "
                            +child.getClass().getName());
                }

                mutableNode.getValue().add(mutableAscendant);
                originalToCopy.put(child, mutableAscendant);
            }
            mutableNode.init();
        }

        return mutableRoot;
    }

    /**
     * @param node root of original tree
     * @param originalToCopyArg (optional) empty map, where binding between original and copy
     * will be stored
     * @return copy of given node and all subnodes recursively
     */
    public static CompositeNode copyDeepAsImmutable(final CompositeNode node,
            final Map<Node<?>, Node<?>> originalToCopyArg) {
        final Deque<CompositeNode> jobQueue = new ArrayDeque<>();
        jobQueue.push(node);

        Map<Node<?>, Node<?>> originalToCopy = originalToCopyArg;
        if (originalToCopy == null) {
            originalToCopy = new HashMap<>();
        }

        while (!jobQueue.isEmpty()) {
            CompositeNode jobNode = jobQueue.peek();
            if (!originalToCopy.isEmpty()
                    && originalToCopy.keySet().containsAll(jobNode.getValue())) {
                jobQueue.pop();
                List<Node<?>> newChildren = NodeUtils.collectMapValues(jobNode.getValue(), originalToCopy);
                CompositeNode nodeCopy = createImmutableCompositeNode(jobNode.getNodeType(), null,
                        newChildren, jobNode.getModificationAction());
                NodeUtils.fixChildrenRelation(nodeCopy);
                originalToCopy.put(jobNode, nodeCopy);
            } else {
                for (Node<?> child : jobNode.getValue()) {
                    if (child instanceof SimpleNode<?>) {
                        originalToCopy.put(child, createImmutableSimpleNode(
                                child.getNodeType(), null, child.getValue(),
                                ((NodeModification) child).getModificationAction()));
                    } else if (child instanceof CompositeNode) {
                        jobQueue.push((CompositeNode) child);
                    }
                }
            }
        }

        return (CompositeNode) originalToCopy.get(node);
    }

}
