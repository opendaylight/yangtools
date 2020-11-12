/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Collection;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * Node which is not leaf, but has child {@link NormalizedNode}s as its value.
 *
 * <p>
 * NormalizedNodeContainer does not have a value, but it has a child nodes. Definition of possible and valid child nodes
 * is introduced in subclasses of this interface.
 *
 * <p>
 * This interface should not be used directly, but rather use of of derived subclasses such as
 * {@link DataContainerNode}, {@link MapNode}, {@link LeafSetNode}.
 *
 * @param <I> Node Identifier type
 * @param <K> Child Node Identifier type
 * @param <V> Child Node type
 */
public interface NormalizedNodeContainer<I extends PathArgument, K extends PathArgument, V extends NormalizedNode>
        extends NormalizedNode {
    @Override
    I getIdentifier();

    /**
     * Returns iteration of all child nodes. Order of returned child nodes may be defined by subinterfaces.
     *
     * <p>
     * <b>Implementation Notes:</b>
     * All nodes returned in this iterable, MUST also be accessible via {@link #getChild(PathArgument)} using their
     * associated identifier.
     *
     * @return Iteration of all child nodes
     */
    @Override
    Collection<V> body();

    /**
     * Return the logical size of this container body. The default implementation defers to {@code body().size()}.
     *
     * @return Size of this container's body.
     */
    default int size() {
        return body().size();
    }

    /**
     * Returns child node identified by provided key.
     *
     * @param child Path argument identifying child node
     * @return Optional with child node if child exists. {@link Optional#empty()} if child does not exist.
     */
    Optional<V> getChild(K child);
}
