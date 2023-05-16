/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointLabel;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;

/**
 * Normalized representation of a YANG mount point. This is similar a {@link DataNodeContainer} in that it has children,
 * but we do not want to tie in the other aspects of that interface.
 */
public interface NormalizedMountPoint extends Immutable {
    /**
     * Return the {@code mount-point} label.
     *
     * @return The label of this mount point.
     */
    @NonNull MountPointLabel label();

    /**
     * Return the underlying mount point context.
     *
     * @return Underlying mount point context
     */
    @NonNull MountPointContext context();

    /**
     * Return the set of top-level children in this mount point.
     *
     * @return Immediate children of this mount point.
     */
    @NonNull Map<PathArgument, DataContainerChild> children();

    /*
     * FIXME: consider whether this interface should contain some information based on 'parent-reference':
     *        - List<YangXPathExpression.QualifiedBound> getParentReference()
     *        - the node-set required to maintain referential integrity in the subtree of this node
     */
}
