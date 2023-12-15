/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * A tuple of normalized {@link #data()}, with corresponding {@link #metadata()} and {@link #mountPoints()}. The three
 * views are expected to be consistent in their addressing -- i.e. when traversing {@link #data()} tree,
 * the corresponding metadata should be available through {@link NormalizedMetadata#getChildren()} and mount point
 * attachments should be available through {@link NormalizedMountpoints#getChildren()}.
 */
// FIXME: this interface could use a traversal utility which binds together the 'NormalizedNodeContainer' part and
//        the corresponding metadata and/or mount points. Most notably mount points are only defined for ContainerNode
//        and MapEntryNode.
@Beta
public interface NormalizedTuple<T extends NormalizedNode> extends Immutable {
    /**
     * Return the data portion of this tree.
     *
     * @return Data portion of this tree.
     */
    @NonNull T data();

    /**
     * Return the metadata portion of this tree. This portion is optional.
     *
     * @return Metadata portion of this tree.
     */
    @Nullable NormalizedMetadata metadata();

    /**
     * Return the mount point portion of this tree. This portion is optional.
     *
     * @return mount point portion of this tree.
     */
    @Nullable NormalizedMountpoints mountPoints();
}
