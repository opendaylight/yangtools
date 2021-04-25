/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;

/**
 * Represents a node that can be added by uses or by augmentation.
 */
@Beta
// FIXME: 8.0.0: refactor this interface to take into account DerivableSchemaNode
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
