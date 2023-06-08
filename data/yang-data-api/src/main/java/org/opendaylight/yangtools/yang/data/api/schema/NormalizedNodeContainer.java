/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

/**
 * Node which is not leaf, but has child {@link NormalizedNode}s as its value. It provides iteration over its child
 * nodes via {@link #body()}. More convenient access to child nodes are provided by {@link DistinctNodeContainer} and
 * {@link OrderedNodeContainer}.
 *
 * @param <V> Child Node type
 */
public sealed interface NormalizedNodeContainer<V extends NormalizedNode> extends NormalizedContainer<V>, NormalizedNode
        permits DistinctNodeContainer, OrderedNodeContainer {
    // Just a composite of NormalizedContainer and NormalizedNode
}
