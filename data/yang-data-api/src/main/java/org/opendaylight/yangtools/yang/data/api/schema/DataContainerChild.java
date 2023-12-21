/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * Marker interface for direct children of {@link DataContainerNode}.
 *
 * <h2>Implementation notes</h2>
 * This interface should not be implemented directly, but rather using one of its subinterfaces:
 *
 * <ul>
 *   <li>{@link ChoiceNode}</li>
 *   <li>{@link ContainerNode}</li>
 *   <li>{@link ForeignDataNode}</li>
 *   <li>{@link LeafNode}</li>
 *   <li>{@link LeafSetNode}</li>
 *   <li>{@link MapNode} and its specializations</li>
 *   <li>{@link UnkeyedListNode}</li>
 * </ul>
 */
public sealed interface DataContainerChild extends NormalizedNode
        permits ChoiceNode, ContainerNode, ForeignDataNode, LeafNode, LeafSetNode, MapNode, UnkeyedListNode {
    @Override
    NodeIdentifier name();
}
