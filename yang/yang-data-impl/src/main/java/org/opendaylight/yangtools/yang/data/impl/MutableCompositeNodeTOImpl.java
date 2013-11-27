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
public class MutableCompositeNodeTOImpl extends AbstractNodeTO<List<Node<?>>> implements MutableCompositeNode {

    private Map<QName, List<Node<?>>> nodeMap = new HashMap<>();
    private CompositeNode original;

    public MutableCompositeNodeTOImpl(QName qname, CompositeNode parent, List<Node<?>> value, ModifyAction modifyAction) {
        super(qname, parent, value, modifyAction);
        init();
    }

    /**
     * update nodeMap - it should be invoked after children was changed
     */
    @Override
    public void init() {
        if (!getChildren().isEmpty()) {
            nodeMap = NodeUtils.buildNodeMap(getChildren());
        }
    }

    @Override
    public List<Node<?>> getChildren() {
        return getValue() == null ? new ArrayList<Node<?>>() : getValue();
    }

    @Override
    public List<Node<?>> setValue(List<Node<?>> value) {
        List<Node<?>> oldVal = super.setValue(value);
        init();
        return oldVal;
    }

    @Override
    public void setModifyAction(ModifyAction action) {
        super.setModificationAction(action);
    }

    protected Map<QName, List<Node<?>>> getNodeMap() {
        return nodeMap;
    }

    @Override
    public MutableCompositeNode asMutable() {
        return this;
    }

    @Override
    public CompositeNode getOriginal() {
        return original;
    }

    /**
     * @param original
     *            the original to set
     */
    public void setOriginal(CompositeNode original) {
        this.original = original;
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
        if (toFilter == null) {
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
        if (toFilter == null) {
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
