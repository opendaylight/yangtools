/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.MountPointLabel;

/**
 * Normalized representation of a YANG mount point. This is a {@link NormalizedTuple}, with a {@link #label()} and the
 * corresponding {@link #context()}. Furthermore {@link #data()} is guaranteed to point at a {@link ContainerNode}.
 */
@NonNullByDefault
public interface NormalizedMountPoint extends NormalizedTuple<ContainerNode> {
    /**
     * Return the {@code mount-point} label.
     *
     * @return The label of this mount point.
     */
    MountPointLabel label();

    /**
     * Return the underlying mount point context.
     *
     * @return Underlying mount point context
     */
    MountPointContext context();

    /*
     * FIXME: consider whether this interface should contain some information based on 'parent-reference':
     *        - List<YangXPathExpression.QualifiedBound> getParentReference()
     *        - the node-set required to maintain referential integrity in the subtree of this node
     */
}
