/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;

/**
 * @author michal.rehak
 * 
 */
public class CompositeNodeTOImpl extends AbstractNodeTO<List<Node<?>>> implements CompositeNode {

    private Map<QName, List<Node<?>>> nodeMap = new HashMap<>();

    /**
     * @param qname
     * @param parent
     *            use null to create top composite node (without parent)
     * @param value
     */
    public CompositeNodeTOImpl(QName qname, CompositeNode parent, List<Node<?>> value) {
        super(qname, parent, value);
        init();
    }

    /**
     * @param qname
     * @param parent
     *            use null to create top composite node (without parent)
     * @param value
     * @param modifyAction
     */
    public CompositeNodeTOImpl(QName qname, CompositeNode parent, List<Node<?>> value, ModifyAction modifyAction) {
        super(qname, parent, value, modifyAction);
        init();
    }
    
    protected void init() {
        if (getValue() != null) {
            nodeMap = NodeUtils.buildNodeMap(getValue());
        }
    }

    protected Map<QName, List<Node<?>>> getNodeMap() {
        return nodeMap;
    }

    @Override
    public List<Node<?>> getChildren() {
        return Collections.unmodifiableList(getValue() == null ? new ArrayList<Node<?>>() : getValue());
    }

    @Override
    public SimpleNode<?> getFirstSimpleByName(QName leafQName) {
        List<SimpleNode<?>> list = getSimpleNodesByName(leafQName);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<CompositeNode> getCompositesByName(QName children) {
        List<Node<?>> toFilter = getNodeMap().get(children);
        if(toFilter == null) {
            return Collections.emptyList();
        }
        List<CompositeNode> list = new ArrayList<CompositeNode>();
        for (Node<?> node : toFilter) {
            if (node instanceof CompositeNode) {
                list.add((CompositeNode) node);
            }
        }
        return list;
    }

    @Override
    public List<SimpleNode<?>> getSimpleNodesByName(QName children) {
        List<Node<?>> toFilter = getNodeMap().get(children);
        if(toFilter == null) {
            return Collections.emptyList();
        }
        List<SimpleNode<?>> list = new ArrayList<SimpleNode<?>>();

        for (Node<?> node : toFilter) {
            if (node instanceof SimpleNode<?>) {
                list.add((SimpleNode<?>) node);
            }
        }
        return list;
    }

    @Override
    public CompositeNode getFirstCompositeByName(QName container) {
        List<CompositeNode> list = getCompositesByName(container);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * @param leaf
     * @return TODO:: do we need this method?
     */
    public SimpleNode<?> getFirstLeafByName(QName leaf) {
        List<SimpleNode<?>> list = getSimpleNodesByName(leaf);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<CompositeNode> getCompositesByName(String children) {
        return getCompositesByName(new QName(getNodeType(), children));
    }

    @Override
    public List<SimpleNode<?>> getSimpleNodesByName(String children) {
        return getSimpleNodesByName(new QName(getNodeType(), children));
    }

    @Override
    public MutableCompositeNode asMutable() {
        throw new IllegalAccessError("cast to mutable is not supported - " + getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return super.toString() + ", children.size = " + (getChildren() != null ? getChildren().size() : "n/a");
    }
    
    @Override
    public void clear() {
        nodeMap.clear();
    }
    
    @Override
    public boolean containsKey(Object key) {
        return nodeMap.containsKey(key);
    }
    
    @Override
    public boolean containsValue(Object value) {
        return nodeMap.containsValue(value);
    }
    
    @Override
    public Set<java.util.Map.Entry<QName, List<Node<?>>>> entrySet() {
        return nodeMap.entrySet();
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
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
    public List<Node<?>> get(Object key) {
        return nodeMap.get(key);
    }

    @Override
    public List<Node<?>> put(QName key, List<Node<?>> value) {
        return nodeMap.put(key, value);
    }

    @Override
    public List<Node<?>> remove(Object key) {
        return nodeMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends QName, ? extends List<Node<?>>> m) {
        nodeMap.putAll(m);
    }

    @Override
    public Set<QName> keySet() {
        return nodeMap.keySet();
    }

    @Override
    public Collection<List<Node<?>>> values() {
        return nodeMap.values();
    }
}
