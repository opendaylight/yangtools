/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;

import com.google.common.collect.Iterables;

import static com.google.common.base.Preconditions.*;

public abstract class AbstractCompositeNodeBuilder<P extends CompositeNode> //
        extends AbstractNodeBuilder<P, CompositeNodeBuilder<P>> //
        implements CompositeNodeBuilder<P> {

    final List<Node<?>> childNodes;

    public AbstractCompositeNodeBuilder() {
        super();
        childNodes = new ArrayList<>();
    }

    public AbstractCompositeNodeBuilder(QName nodeType, Map<QName, String> attributes) {
        super(nodeType, attributes);
        childNodes = new ArrayList<>();
    }

    public AbstractCompositeNodeBuilder(QName nodeType, Iterable<? extends Node<?>> nodes) {
        super(nodeType);
        childNodes = new ArrayList<>();
    }

    @Override
    public AbstractCompositeNodeBuilder<P> add(Node<?> node) {
        childNodes.add(checkNotNull(node, "Node should not be null"));
        return this;
    }

    @Override
    public AbstractCompositeNodeBuilder<P> addAll(Iterable<? extends Node<?>> nodes) {
        Iterables.addAll(childNodes, checkNotNull(nodes, "Node should not be null"));
        return this;
    }

    @Override
    public CompositeNodeBuilder<P> addLeaf(String leafLocalName, String leafValue) {
        return addLeaf(QName.create(getQName(), leafLocalName), leafValue);
    }

    public List<Node<?>> getChildNodes() {
        return childNodes;
    }
}
