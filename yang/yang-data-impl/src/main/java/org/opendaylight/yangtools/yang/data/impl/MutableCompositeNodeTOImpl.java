/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * @author michal.rehak
 *
 * @deprecated Use one of the {@link NormalizedNodeContainer} implementations instead.
 */
@Deprecated
public class MutableCompositeNodeTOImpl extends AbstractNodeTO<List<Node<?>>> implements MutableCompositeNode, Serializable {

    private static final long serialVersionUID = 100L;

    private Map<QName, List<Node<?>>> nodeMap = new HashMap<>();
    private CompositeNode original;

    public MutableCompositeNodeTOImpl(final QName qname, final CompositeNode parent, final List<Node<?>> value, final ModifyAction modifyAction) {
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
    public List<Node<?>> setValue(final List<Node<?>> value) {
        List<Node<?>> oldVal = super.setValue(value);
        init();
        return oldVal;
    }

    @Override
    public void setModifyAction(final ModifyAction action) {
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
    public void setOriginal(final CompositeNode original) {
        this.original = original;
    }

    @Override
    public SimpleNode<?> getFirstSimpleByName(final QName leafQName) {
        List<SimpleNode<?>> list = getSimpleNodesByName(leafQName);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<CompositeNode> getCompositesByName(final QName children) {
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
    public List<SimpleNode<?>> getSimpleNodesByName(final QName children) {
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
    public CompositeNode getFirstCompositeByName(final QName container) {
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
    public SimpleNode<?> getFirstLeafByName(final QName leaf) {
        List<SimpleNode<?>> list = getSimpleNodesByName(leaf);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<CompositeNode> getCompositesByName(final String children) {
        return getCompositesByName(QName.create(getNodeType(), children));
    }

    @Override
    public List<SimpleNode<?>> getSimpleNodesByName(final String children) {
        return getSimpleNodesByName(QName.create(getNodeType(), children));
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
    public boolean containsKey(final Object key) {
        return nodeMap.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return nodeMap.containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<QName, List<Node<?>>>> entrySet() {
        return nodeMap.entrySet();
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
    public Set<QName> keySet() {
        return nodeMap.keySet();
    }

    @Override
    public Collection<List<Node<?>>> values() {
        return nodeMap.values();
    }

    // Serialization related

    private void readObject(final ObjectInputStream aStream) throws IOException, ClassNotFoundException {
        aStream.defaultReadObject();
        QName qName = (QName)aStream.readObject();
        CompositeNode parent = (CompositeNode) aStream.readObject();
        @SuppressWarnings("unchecked")
        List<Node<?>> value = (List<Node<?>>) aStream.readObject();
        ModifyAction modifyAction = (ModifyAction) aStream.readObject();

        init(qName, parent, value, modifyAction);
    }

    private void writeObject(final ObjectOutputStream aStream) throws IOException {
        aStream.defaultWriteObject();
        //manually serialize superclass
        aStream.writeObject(getQName());
        aStream.writeObject(getParent());
        aStream.writeObject(getValue());
        aStream.writeObject(getModificationAction());
    }
}
