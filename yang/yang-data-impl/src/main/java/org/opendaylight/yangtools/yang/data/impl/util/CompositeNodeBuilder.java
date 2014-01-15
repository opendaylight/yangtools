/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.util;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;

public interface CompositeNodeBuilder<P extends CompositeNode> extends NodeBuilder<P,CompositeNodeBuilder<P>> {

    CompositeNodeBuilder<P> addLeaf(QName leafName,Object leafValue);
    CompositeNodeBuilder<P> add(Node<?> node);
    CompositeNodeBuilder<P> addAll(Iterable<? extends Node<?>> nodes);
    CompositeNodeBuilder<P> addLeaf(String leafLocalName, String leafValue);
}
