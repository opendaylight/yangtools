/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

/**
 * Child ordering policy. It defines how a {@link ModifiedNode} tracks its children.
 */
enum ChildTrackingPolicy {
    /**
     * No child nodes are possible, ever.
     */
    NONE,
    /**
     * Child nodes are possible and we need to make sure that their iteration order
     * matches the order in which they are introduced.
     */
    ORDERED,
    /**
     * Child nodes are possible, but their iteration order can be undefined.
     */
    UNORDERED,
}
