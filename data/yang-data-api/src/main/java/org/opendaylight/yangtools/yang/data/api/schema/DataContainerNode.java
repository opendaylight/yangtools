/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * Abstract node which does not have value but contains valid {@link DataContainerChild} nodes. Schema of this node is
 * described by instance of {@link org.opendaylight.yangtools.yang.model.api.DataNodeContainer}.
 *
 * <h2>Implementation notes</h2>
 * This interface should not be implemented directly, but rather implementing one of it's subclasses
 * <ul>
 *   <li>{@link ChoiceNode}</li>
 *   <li>{@link ContainerNode}</li>
 *   <li>{@link MapEntryNode} and its specializations</li>
 *   <li>{@link UnkeyedListEntryNode}</li>
 * </ul>
 */
public non-sealed interface DataContainerNode
        extends DataContainer, DistinctNodeContainer<NodeIdentifier, DataContainerChild> {
}
