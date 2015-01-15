/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.MutableCompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.data.impl.util.AbstractCompositeNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.util.CompositeNodeBuilder;

/**
 * @deprecated Use one of the {@link NormalizedNodeContainer} implementations.
 */
@Deprecated
public final class ImmutableCompositeNode extends AbstractNodeTO<List<Node<?>>> implements Immutable, CompositeNode, AttributesContainer, Serializable {

    private static final long serialVersionUID = 100L;

    private Map<QName, List<Node<?>>> nodeMap = new HashMap<>();

    private final Map<QName, String> attributes;



    /**
     * @param qname
     * @param parent
     *            use null to create top composite node (without parent)
     * @param value
     */
    private ImmutableCompositeNode(final QName qname, final Map<QName,String> attributes,final List<Node<?>> value) {
        super(qname, null, ImmutableList.copyOf(value));
        if(attributes == null) {
            this.attributes = ImmutableMap.<QName, String>of();
        } else {
            this.attributes = ImmutableMap.copyOf(attributes);
        }
        init();
    }

    /**
     * @param qname
     * @param value
     * @param modifyAction
     */
    public ImmutableCompositeNode(final QName qname, final List<Node<?>> value, final ModifyAction modifyAction) {
        super(qname, null, value, modifyAction);
        attributes = ImmutableMap.of();
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

    @Override
    public Map<QName, String> getAttributes() {
        return attributes;
    }

    @Override
    public String getAttributeValue(final QName key) {
        return attributes.get(key);
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
    public MutableCompositeNode asMutable() {
        throw new IllegalAccessError("cast to mutable is not supported - " + getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return super.toString() + ", children.size = " + (!getChildren().isEmpty() ? getChildren().size() : "n/a");
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
        QName qName = (QName) aStream.readObject();
        CompositeNode parent = (CompositeNode) aStream.readObject();
        @SuppressWarnings("unchecked")
        List<Node<?>> value = (List<Node<?>>) aStream.readObject();
        ModifyAction modifyAction = (ModifyAction) aStream.readObject();

        init(qName, parent, value, modifyAction);
    }

    private void writeObject(final ObjectOutputStream aStream) throws IOException {
        aStream.defaultWriteObject();
        // manually serialize superclass
        aStream.writeObject(getQName());
        aStream.writeObject(getParent());
        aStream.writeObject(getValue());
        aStream.writeObject(getModificationAction());
    }

    public static CompositeNodeBuilder<ImmutableCompositeNode> builder() {
        return new ImmutableCompositeNodeBuilder();
    }

    private static class ImmutableCompositeNodeBuilder extends AbstractCompositeNodeBuilder<ImmutableCompositeNode> {

        @Override
        public AbstractCompositeNodeBuilder<ImmutableCompositeNode> addLeaf(final QName leafName, final Object leafValue) {
            add(new SimpleNodeTOImpl<Object>(leafName, null, leafValue));
            return this;
        }

        @Override
        public ImmutableCompositeNode build() {
            return ImmutableCompositeNode.create(this.getQName(), this.getAttributes(), this.getChildNodes());
        }

        @Deprecated
        public ImmutableCompositeNode toInstance() {
            return ImmutableCompositeNode.create(this.getQName(), this.getAttributes(), this.getChildNodes());
        }

    }

    public static ImmutableCompositeNode create(final QName qName, final List<Node<?>> childNodes) {
        return new ImmutableCompositeNode(qName, ImmutableMap.<QName, String>of(),childNodes);
    }

    public static ImmutableCompositeNode create(final QName qName, final Map<QName, String> attributes, final List<Node<?>> childNodes) {
        return new ImmutableCompositeNode(qName, attributes,childNodes);
    }

    public static ImmutableCompositeNode create(final QName qName, final List<Node<?>> childNodes, final ModifyAction modifyAction) {
        return new ImmutableCompositeNode(qName, childNodes, modifyAction);
    }
}
