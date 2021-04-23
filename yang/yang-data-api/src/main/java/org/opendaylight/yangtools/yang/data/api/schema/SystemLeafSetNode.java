/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

/**
 * Node representing set of simple leaf nodes. Node containing instances of {@link LeafSetEntryNode}.
 *
 * <p>
 * Schema and semantics of this node are described by instance of
 * {@link org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode}.
 *
 * @param <T> Type of leaf node values.
 */
public interface SystemLeafSetNode<T> extends LeafSetNode<T>, OrderingAware.System {
    @Override
    @SuppressWarnings("rawtypes")
    default Class<SystemLeafSetNode> contract() {
        return SystemLeafSetNode.class;
    }

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);
}
