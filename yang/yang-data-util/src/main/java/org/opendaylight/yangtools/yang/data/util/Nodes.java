/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.MutableSimpleNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * @deprecated Use one of the {@link NormalizedNode} implementation packages.
 */
@Deprecated
public final class Nodes {

    private Nodes() {
    }

    public static <T> SimpleNode<T> leafNode(final QName name, final T value) {
        return new SimpleNodeTO<T>(name, value, null);
    }

    public static CompositeNode containerNode(final QName name, final List<Node<?>> children) {
        return containerNode(name, children, null);
    }

    public static CompositeNode containerNode(final QName name, final List<Node<?>> children, final CompositeNode parent) {
        return new ContainerNodeTO(name, parent, nodeMapFromList(children));
    }

    public static Map<QName, List<Node<?>>> nodeMapFromList(final List<Node<?>> children) {
        Map<QName, List<Node<?>>> map = new HashMap<QName, List<Node<?>>>();
        for (Node<?> node : children) {

            QName name = node.getNodeType();
            List<Node<?>> targetList = map.get(name);
            if (targetList == null) {
                targetList = new ArrayList<Node<?>>();
                map.put(name, targetList);
            }
            targetList.add(node);
        }
        return map;
    }

    /**
     * @deprecated Use one of the {@link NormalizedNode} implementation packages.
     */
    @Deprecated
    private static class ContainerNodeTO extends AbstractContainerNode {

        private final Map<QName, List<Node<?>>> nodeMap;

        public ContainerNodeTO(final QName name, final Map<QName, List<Node<?>>> nodeMap) {
            super(name);
            this.nodeMap = nodeMap;
        }

        public ContainerNodeTO(final QName name, final CompositeNode parent, final Map<QName, List<Node<?>>> nodeMap) {
            super(name, parent);
            this.nodeMap = nodeMap;
        }

        @Override
        protected Map<QName, List<Node<?>>> getNodeMap() {

            return nodeMap;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.opendaylight.yangtools.yang.data.api.CompositeNode#asMutable()
         */
        @Override
        public MutableCompositeNode asMutable() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public QName getKey() {
            return getNodeType();
        }

        @Override
        public List<Node<?>> setValue(final List<Node<?>> value) {
            return null;
        }

        @Override
        public int size() {
            return nodeMap.size();
        }

        @Override
        public boolean isEmpty() {
            return nodeMap.isEmpty();
        }

        @Override
        public boolean containsKey(final Object key) {
            return nodeMap.containsKey(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            return nodeMap.containsValue(value);
        }

        @Override
        public List<Node<?>> get(final Object key) {
            return nodeMap.get(key);
        }

        @Override
        public List<Node<?>> put(final QName key, final List<Node<?>> value) {
            return nodeMap.put(key, value);
        }

        @Override
        public List<Node<?>> remove(final Object key) {
            return nodeMap.remove(key);
        }

        @Override
        public void putAll(final Map<? extends QName, ? extends List<Node<?>>> m) {
            nodeMap.putAll(m);
        }

        @Override
        public void clear() {
            nodeMap.clear();
        }

        @Override
        public Set<QName> keySet() {
            return nodeMap.keySet();
        }

        @Override
        public Collection<List<Node<?>>> values() {
            return nodeMap.values();
        }

        @Override
        public Set<java.util.Map.Entry<QName, List<Node<?>>>> entrySet() {

            return nodeMap.entrySet();
        }

    }

    /**
     * @deprecated Use one of the {@link NormalizedNode} implementation packages.
     */
    @Deprecated
    private static class SimpleNodeTO<T> extends AbstractNode<T> implements SimpleNode<T> {

        private final T value;

        protected SimpleNodeTO(final QName name, final T val, final CompositeNode parent) {
            super(name, parent);
            value = val;

        }

        @Override
        public T getValue() {
            return value;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.opendaylight.yangtools.yang.data.api.SimpleNode#asMutable()
         */
        @Override
        public MutableSimpleNode<T> asMutable() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public T setValue(final T value) {
            return null;
        }

        @Override
        public QName getKey() {
            return getNodeType();
        }
    }

}
