/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Represents a node that can be added by uses or by augmentation.
 *
 * @deprecated Aside from the deprecated {@link AddedByUsesAware} contract, this interface adds only a trait related
 *             to now how we arrived at this effective node. Users who need to know this information should really be
 *             looking at the {@link DeclaredStatement} world, which holds the original node definition.
 */
@Deprecated
public interface CopyableNode extends AddedByUsesAware {
    /**
     * Returns <code>true</code> if this node was added by augmentation,
     * otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if this node was added by augmentation,
     *         otherwise returns <code>false</code>
     */
    boolean isAugmenting();
}
