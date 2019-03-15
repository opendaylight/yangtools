/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Abstract node which does not have value but contains valid {@link DataContainerChild} nodes.
 * Schema of this node is described by instance of {@link org.opendaylight.yangtools.yang.model.api.DataNodeContainer}.
 *
 * <p>
 * <h2>Implementation notes</h2>
 * This interface should not be implemented directly, but rather implementing one of it's subclasses
 * <ul>
 * <li>{@link ContainerNode}
 * <li>{@link MapEntryNode}
 * <li>{@link UnkeyedListEntryNode}
 * <li>{@link ChoiceNode}
 * <li>{@link AugmentationNode}
 * </ul>
 *
 * @param <K> {@link PathArgument} which identifies instance of {@link DataContainerNode}
 */
public interface DataContainerNode<K extends PathArgument> extends
        NormalizedNodeContainer<K, PathArgument, DataContainerChild<? extends PathArgument, ?>> {
    /**
     * Returns iteration of all child nodes.
     * Order of returned child nodes may be defined by subinterfaces.
     *
     * <p>
     * <b>Implementation Notes:</b>
     * All nodes returned in this iterable, MUST also be accessible via
     * {@link #getChild(PathArgument)} using their associated identifier.
     *
     * @return Iteration of all child nodes
     */
    @Override
    Collection<DataContainerChild<? extends PathArgument, ?>> getValue();
}
