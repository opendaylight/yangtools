/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

/**
 * The ContainerSchemaNode is used to define an interior data node in the schema tree. There are two styles of
 * containers, those that exist only for organizing the hierarchy of data nodes, and those whose presence in the
 * configuration has an explicit meaning.
 */
public interface ContainerSchemaNode extends ContainerLike, EffectiveStatementEquivalent {
    @Override
    ContainerEffectiveStatement asEffectiveStatement();

    /**
     * Returns true if this container is marked as presence.
     *
     * @return true, if presence of this container has an explicit meaning,
     *         false otherwise
     */
    boolean isPresenceContainer();
}
