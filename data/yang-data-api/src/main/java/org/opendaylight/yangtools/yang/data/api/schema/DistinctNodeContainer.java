/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A {@link NormalizedNodeContainer} which contains directly-addressable children. It
 *
 * <p>
 * NormalizedNodeContainer does not have a value, but it has a child nodes. Definition of possible and valid child nodes
 * is introduced in subclasses of this interface.
 *
 * <p>
 * This interface should not be used directly, but rather use of of derived subclasses such as
 * {@link DataContainerNode}, {@link MapNode}, {@link LeafSetNode}.
 *
 * @param <K> Child path argument type
 * @param <V> Child Node type
 */
public sealed interface DistinctNodeContainer<K extends PathArgument, V extends NormalizedNode>
        extends DistinctContainer<K, V>, NormalizedNodeContainer<V>
        permits DataContainerNode, LeafSetNode, MapNode {
    // Composition of DistinctContainer and NormalizedNodeContainer
}
