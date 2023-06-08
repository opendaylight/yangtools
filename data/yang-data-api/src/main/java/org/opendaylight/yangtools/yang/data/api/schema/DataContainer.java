/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A {@link DistinctContainer} containing {@link DataContainerChild} children.
 *
 * <p>
 * <b>NOTE:</b>
 * All implementations of this interface are assumed to be {@link OrderingAware.System}, i.e. order-independent.
 */
public sealed interface DataContainer
        extends DistinctContainer<NodeIdentifier, DataContainerChild>, OrderingAware.System
        permits DataContainerNode, NormalizedYangData {
    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);
}
