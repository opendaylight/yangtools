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
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * @deprecated Use one of the {@link NormalizedNodeContainer} implementation packages.
 */
@Deprecated
public abstract class AbstractContainerNode extends AbstractNode<List<Node<?>>> implements CompositeNode {

    @Override
    public SimpleNode<?> getFirstSimpleByName(final QName leaf) {
        List<SimpleNode<?>> list = getSimpleNodesByName(leaf);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    protected AbstractContainerNode(final QName name, final CompositeNode parent) {
        super(name, parent);
    }

    public AbstractContainerNode(final QName name) {
        super(name, null);
    }

    @Override
    public List<Node<?>> getChildren() {
        return getValue();
    }

    @Override
    public List<Node<?>> getValue() {
        Map<QName, List<Node<?>>> map = getNodeMap();
        if (map == null) {
            throw new IllegalStateException("nodeMap should not be null");
        }
        List<Node<?>> ret = new ArrayList<Node<?>>();
        Collection<List<Node<?>>> values = map.values();
        for (List<Node<?>> list : values) {
            ret.addAll(list);
        }
        return ret;
    }

    protected abstract Map<QName, List<Node<?>>> getNodeMap();

    @Override
    public List<CompositeNode> getCompositesByName(final QName children) {
        Map<QName, List<Node<?>>> map = getNodeMap();
        if (map == null) {
            throw new IllegalStateException("nodeMap should not be null");
        }
        List<Node<?>> toFilter = map.get(children);
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
        Map<QName, List<Node<?>>> map = getNodeMap();
        if (map == null) {
            throw new IllegalStateException("nodeMap should not be null");
        }
        List<Node<?>> toFilter = map.get(children);
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
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    public SimpleNode<?> getFirstLeafByName(final QName leaf) {
        List<SimpleNode<?>> list = getSimpleNodesByName(leaf);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<CompositeNode> getCompositesByName(final String children) {
        return getCompositesByName(localQName(children));
    }

    @Override
    public List<SimpleNode<?>> getSimpleNodesByName(final String children) {
        return getSimpleNodesByName(localQName(children));
    }

    private QName localQName(final String str) {
        return QName.create(getNodeType(), str);
    }
}
