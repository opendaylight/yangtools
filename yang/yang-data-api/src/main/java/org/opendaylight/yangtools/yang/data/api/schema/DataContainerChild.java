/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Marker interface for direct children of {@link DataContainerNode}.
 *
 * <h3>Implementation notes</h3>
 * This interface should not be implemented directly, but rather using one
 * of its subinterfaces:
 *
 * <ul>
 * <li>{@link LeafNode}
 * <li>{@link ContainerNode}
 * <li>{@link ChoiceNode}
 * <li>{@link MapNode}
 * <li>{@link AugmentationNode}
 * </ul>
 *
 * @param <K> Path Argument Type which is used to identify node
 * @param <V> Value type
 */
public interface DataContainerChild<K extends PathArgument,V> extends NormalizedNode<K, V> {
    @Override
    K getIdentifier();
}
