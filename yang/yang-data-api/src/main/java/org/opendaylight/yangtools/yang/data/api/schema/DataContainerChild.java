/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

/**
 * Marker interface for direct children of {@link DataContainerNode}.
 *
 * <h2>Implementation notes</h2>
 * This interface should not be implemented directly, but rather using one of its subinterfaces:
 *
 * <ul>
 *   <li>{@link AugmentationNode}
 *   <li>{@link ChoiceNode}
 *   <li>{@link ContainerNode}
 *   <li>{@link ForeignDataNode}
 *   <li>{@link LeafNode}
 *   <li>{@link LeafSetNode}
 *   <li>{@link MapNode}
 *   <li>{@link UnkeyedListNode}
 * </ul>
 */
public interface DataContainerChild extends NormalizedNode {

}
