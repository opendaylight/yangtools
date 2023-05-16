/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * RFC8528 mount point counterpart to a {@link NormalizedNode} structure. This interface represents a tree, navigable
 * using {@link PathArgument}, matching the layout of its NormalizedNode companion. At each node a single
 * {@link NormalizedMountPoint} may be attached -- indicating attachment of externally-modeled data.
 */
public interface NormalizedMountpoints extends Immutable {
    /**
     * Return the {@link NormalizedMountPoint} attached at this place in the tree, if any.
     *
     * @return The set of annotations attached to the corresponding data node.
     */
    @Nullable NormalizedMountPoint mountPoint();

    /**
     * Returns child nodes. Default implementation returns an empty immutable map.
     *
     * @return Child {@link NormalizedMountpoints}s.
     */
    default @NonNull Map<PathArgument, NormalizedMountpoints> getChildren() {
        return ImmutableMap.of();
    }
}
