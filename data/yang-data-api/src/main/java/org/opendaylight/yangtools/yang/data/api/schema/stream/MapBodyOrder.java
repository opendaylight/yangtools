/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import java.io.IOException;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;

/**
 * The order in which {@link MapEntryNode}s of a particular {@link MapNode} should be emitted in. This is a sealed
 * internal implementation detail.
 *
 * @apiNote This contract exposes two singleton implementations, which we could model via an enum. We went the way
 *          of a sealed abstract for two reasons:
 *          - we do not want to expose orderChildren
 *          - we actually do not want to load IterationMapBodyOrder unless requested, so that the JVM does not have
 *            to worry about it unless requested
 *          -
 */
abstract sealed class MapBodyOrder permits DefaultMapBodyOrder, IterationMapBodyOrder {
    /**
     * Order the body of specified node.
     *
     * @param entry map entry
     * @return The body in the order it should be written out
     */
    abstract Iterable<DataContainerChild> orderBody(MapEntryNode entry) throws IOException;
}